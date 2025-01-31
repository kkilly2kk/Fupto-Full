package com.fupto.back.admin.member.service;

import com.fupto.back.admin.member.dto.*;
import com.fupto.back.entity.*;
import com.fupto.back.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service("adminMemberService")
@Transactional
public class DefaultMemberService implements MemberService {

    @Value("${file.upload.path}")
    private String uploadPath;

    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final BoardRepository boardRepository;
    private final FavoriteRepository favoriteRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public DefaultMemberService(MemberRepository memberRepository,
                                ModelMapper modelMapper,
                                BoardRepository boardRepository,
                                FavoriteRepository favoriteRepository,
                                ProductImageRepository productImageRepository,
                                ProductRepository productRepository) {
        this.memberRepository = memberRepository;
        this.modelMapper = modelMapper;
        this.boardRepository = boardRepository;
        this.favoriteRepository = favoriteRepository;
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    @Override
    public MemberResponseDto getMemberList(MemberSearchDto searchDto) {
        Pageable pageable = PageRequest.of(
                searchDto.getPage() - 1,
                searchDto.getSize(),
                Sort.by("id").descending()
        );

        // 검색 조건 설정
        String userId = searchDto.getSearchType().equals("userId") ? searchDto.getSearchKeyWord() : null;
        String nickname = searchDto.getSearchType().equals("nickname") ? searchDto.getSearchKeyWord() : null;
        String email = searchDto.getSearchType().equals("email") ? searchDto.getSearchKeyWord() : null;

        String role = (searchDto.getRole() == null || searchDto.getRole().isEmpty()) ?
                null : searchDto.getRole();
        String gender = (searchDto.getGender() == null || searchDto.getGender().isEmpty()) ?
                null : searchDto.getGender();

        // 날짜 변환
        Instant startDateI = searchDto.getStartDate() != null ?
                LocalDate.parse(searchDto.getStartDate())
                        .atStartOfDay(ZoneId.of("Asia/Seoul"))
                        .toInstant() : null;

        Instant endDateI = searchDto.getEndDate() != null ?
                LocalDate.parse(searchDto.getEndDate())
                        .atTime(LocalTime.MAX)
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toInstant() : null;

        Page<Member> memberPage = memberRepository.searchMembers(
                searchDto.getMemberStatus(),
                role,
                gender,
                userId,
                nickname,
                email,
                searchDto.getDateType(),
                startDateI,
                endDateI,
                pageable
        );

        List<MemberListDto> memberListDtos = memberPage.getContent().stream()
                .map(member -> modelMapper.map(member, MemberListDto.class))
                .toList();

        return MemberResponseDto.builder()
                .totalCount(memberPage.getTotalElements())
                .totalPages((long) memberPage.getTotalPages())
                .hasNextPage(memberPage.hasNext())
                .hasPrevPage(memberPage.hasPrevious())
                .pages(IntStream.rangeClosed(1, memberPage.getTotalPages())
                        .mapToObj(Long::valueOf)
                        .collect(Collectors.toList()))
                .members(memberListDtos)
                .build();
    }

    @Override
    public MemberDetailDto getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        MemberDetailDto memberDetail = modelMapper.map(member, MemberDetailDto.class);

        // 게시글 정보 설정
        List<Board> boards = boardRepository.findByRegMemberId(id);
        List<BoardListDto> boardListDtos = boards.stream()
                .map(this::getBoard)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        memberDetail.setBoardCount(boardListDtos.size());
        memberDetail.setBoardList(boardListDtos);

        // 즐겨찾기 정보 설정
        List<Favorite> favorites = favoriteRepository.findAllByMemberId(id);
        List<FavoriteListDto> favoriteListDtos = favorites.stream()
                .map(this::getFavorite)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        memberDetail.setFavoriteCount(favoriteListDtos.size());
        memberDetail.setFavoriteList(favoriteListDtos);

        return memberDetail;
    }

    @Override
    @Transactional
    public void updateMemberActive(Long id, Boolean active) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if (!member.getState()) {
            throw new IllegalStateException("탈퇴한 회원의 상태는 변경할 수 없습니다.");
        }

        member.setActive(active);
        member.setUpdateDate(Instant.now());
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateMemberRole(Long id, String role) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if (!member.getState() || !member.getActive()) {
            throw new IllegalStateException("비활성화되거나 탈퇴한 회원의 권한은 변경할 수 없습니다.");
        }

        member.setRole(role);
        member.setUpdateDate(Instant.now());
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    @Override
    public FavoriteListDto getFavorite(Favorite favorite) {
        if (favorite == null) {
            return null;
        }
        Product product = productRepository.findById(favorite.getMappingId()).orElse(null);
        if (product == null) {
            return null;
        }

        Brand brand = product.getBrand();
        return FavoriteListDto.builder()
                .id(favorite.getId())
                .productId(product.getId())
                .productName(product.getProductName())
                .memberId(favorite.getMember().getId())
                .memberName(favorite.getMember().getNickname())
                .createDate(favorite.getCreateDate())
                .productBrandName(brand.getEngName())
                .build();
    }

    @Override
    public Resource getProductImage(Long id) throws IOException {
        ProductImage productImage = productImageRepository.findByProductIdAndDisplayOrder(id, 1)
                .orElseThrow(() -> new EntityNotFoundException("해당 순서의 이미지를 찾을 수 없습니다."));

        Path imagePath = Paths.get(uploadPath, productImage.getFilePath());
        Resource resource = new FileSystemResource(imagePath.toFile());

        if (!resource.exists()) {
            throw new FileNotFoundException("이미지 파일을 찾을 수 없습니다.");
        }

        return resource;
    }

    private BoardListDto getBoard(Board board) {
        if (board == null) {
            return null;
        }
        return BoardListDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .build();
    }
}
