package com.fupto.back.user.member.service;

import com.fupto.back.entity.*;
import com.fupto.back.repository.*;
import com.fupto.back.user.member.dto.*;
import com.fupto.back.user.member.exception.InvalidPasswordException;
import com.fupto.back.user.member.exception.PasswordMismatchException;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service("userMemberService")
@Transactional
public class DefaultMemberService implements MemberService {

    @Value("${file.upload.path}")
    private String uploadPath;

    private final BoardRepository boardRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;
    private final ProductImageRepository productImageRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final CommentRepository commentRepository;

    public DefaultMemberService(MemberRepository memberRepository,
                                ModelMapper modelMapper,
                                PasswordEncoder passwordEncoder,
                                ProductImageRepository productImageRepository,
                                FavoriteRepository favoriteRepository,
                                ProductRepository productRepository,
                                PriceHistoryRepository priceHistoryRepository,
                                BoardRepository boardRepository,
                                CommentRepository commentRepository) {
        this.memberRepository = memberRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.productImageRepository = productImageRepository;
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
    }


    @Transactional
    public MemberResponseDto editSocialMember(Member member, MemberEditDto dto) {
        updateMemberInfo(member, dto);
        Member savedMember = memberRepository.save(member);
        return modelMapper.map(savedMember, MemberResponseDto.class);
    }

    // 일반 사용자 정보 수정
    @Transactional
    public MemberResponseDto editMember(Member member, MemberEditDto dto) {
        // 비밀번호 검증
        validatePassword(member, dto.getPassword());
        // 새 비밀번호가 있는 경우 처리
        updatePasswordIfProvided(member, dto);
        // 나머지 정보 업데이트
        updateMemberInfo(member, dto);

        Member savedMember = memberRepository.save(member);
        return modelMapper.map(savedMember, MemberResponseDto.class);
    }

    // 회원 조회 메서드
    public Member findByUserId(String userId) {
        if (!memberRepository.existsByUserId(userId)) {
            throw new EntityNotFoundException("회원이 존재하지 않습니다.");
        }
        return memberRepository.findOptionalByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(userId + "가 존재하지 않습니다."));
    }

    // 회원 정보 업데이트 공통 메서드
    private void updateMemberInfo(Member member, MemberEditDto dto) {
        if (StringUtils.hasText(dto.getNickname())) {
            member.setNickname(dto.getNickname());
        }

        if (StringUtils.hasText(dto.getTel())) {
            member.setTel(dto.getTel());
        }

        if (StringUtils.hasText(dto.getGender())) {
            member.setGender(dto.getGender());
        }

        if (StringUtils.hasText(dto.getBirthDate())) {
            updateBirthDate(member, dto.getBirthDate());
        }

        member.setUpdateDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toInstant(ZoneOffset.UTC));
    }

    // 비밀번호 검증
    private void validatePassword(Member member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new InvalidPasswordException("입력하신 비밀번호가 일치하지 않습니다.");
        }
    }

    // 새 비밀번호 업데이트
    private void updatePasswordIfProvided(Member member, MemberEditDto dto) {
        if (StringUtils.hasText(dto.getNewPassword())) {
            if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
                throw new PasswordMismatchException("새 비밀번호가 일치하지 않습니다.");
            }
            member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }
    }

    // 생년월일 업데이트
    private void updateBirthDate(Member member, String birthDate) {
        try {
            LocalDate localDate = LocalDate.parse(birthDate);
            member.setBirthDate(localDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 날짜 형식입니다.");
        }
    }

    @Override
    public MemberResponseDto getMember(Long id) {
        if (memberRepository.findById(id) == null)
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");

        Optional<Member> member = memberRepository.findById(id);

        return modelMapper.map(member, MemberResponseDto.class);
    }

    //--------------favorite 관련 매서드-------------------------
    @Override
    public Resource getProductImage(Long id) throws IOException {
        Integer order = 1;
        ProductImage productImage = productImageRepository.findByProductIdAndDisplayOrder(id, order)
                .orElseThrow( () -> new EntityNotFoundException("해당 순서의 이미지를 찾을 수 없습니다."));
        Path imagePath = Paths.get(uploadPath, productImage.getFilePath());
        Resource resource = new FileSystemResource(imagePath.toFile());

        if (!resource.exists()){
            throw new FileNotFoundException("이미지 파일을 찾을 수 없습니다.");
        }


        return resource;
    }
    @Override
    public List<FavoriteListDto> getFavorites(Long id) {
        List<Favorite> favorites = favoriteRepository.findAllByMemberIdAndStateIsTrueOrderByUpdateDateDesc(id);
        if (favorites == null || favorites.isEmpty()){
            return Collections.emptyList();
        }
        List<FavoriteListDto> dtoList = new ArrayList<>();
        for (Favorite favorite : favorites){
            Integer priceHistories = priceHistoryRepository.findLowestCurrentPrice(favorite.getMappingId());
            Product product = productRepository.findById(favorite.getMappingId()).orElse(null);
            if (product == null){
                continue;
            }
            Brand brand = product.getBrand();

            FavoriteListDto dto = new FavoriteListDto();
            dto.setId(favorite.getId());
            dto.setProductId(product.getId());
            dto.setProductName(product.getProductName());
            dto.setProductPrice(priceHistories);
            dto.setMemberId(favorite.getMember().getId());
            dto.setMemberName(favorite.getMember().getNickname());
            dto.setCreateDate(favorite.getCreateDate());
            dto.setProductBrandName(brand.getEngName());
            dto.setAlertPrice(favorite.getAlertPrice());

            dtoList.add(dto);
        }
        return dtoList;
    }

    //------board 관련 매서드-------------------------
    @Override
    public List<BoardListDto> getBoards(Long memberId) {
        List<Board> boards = boardRepository.findAllByRegMemberIdAndActiveIsTrue(memberId);


        return boards.stream().map(this::convertToBoardListDto)
                .collect(Collectors.toList());
    }

    @Override
    public Resource getBoardImage(Long id) throws IOException {
        Optional<Board> board = boardRepository.findById(id);
        String boardImg = board.map(Board::getImg).orElse("defaultImgPath");
        Path imagePath = Paths.get(boardImg);
        Resource resource = new FileSystemResource(imagePath.toFile());
        if (!resource.exists()){
            throw new FileNotFoundException("이미지 파일을 찾을 수 없습니다.");
        }
        return resource;
    }

    private BoardListDto convertToBoardListDto(Board board){
        return BoardListDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .img(board.getImg())
                .regMemberId(board.getRegMember().getId())
                .regMemberNickName(board.getRegMember().getNickname())
                .regMemberProfileImg(board.getRegMember().getProfileImg())
                .createdAt(board.getCreateDate())
                .active(board.getActive())
                .commentCount(commentRepository.countByBoardId(board.getId()))
                .build();
    }

    @Transactional
    @Override
    public void updateAlertPrice(Long memberId, Long mappingId, Integer alertPrice) {

        Favorite favorite = favoriteRepository.findByMemberIdAndMappingId(memberId, mappingId)
                .orElseThrow(() -> new EntityNotFoundException("Favorite not found"));

        Integer oldAlertPrice = favorite.getAlertPrice();
        favorite.setAlertPrice(alertPrice);
        favoriteRepository.save(favorite);
    }


    @Transactional
    public void updateProfileImage(Long memberId, MultipartFile file) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 기본 경로 설정 (절대 경로)
        Path basePath = Paths.get(uploadPath).toAbsolutePath();
        String uploadDir = basePath.resolve("user").resolve(memberId.toString()).toString();
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 파일명 생성
        String filename = UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());
        File targetFile = new File(directory, filename);

        // 기존 이미지가 있다면 삭제
        if (member.getProfileImg() != null) {
            File oldFile = new File(member.getProfileImg());
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        // 새 이미지 저장
        file.transferTo(targetFile);
        // DB에 상대 경로 저장
        String relativePath = String.format("uploads/user/%d/%s", memberId, filename);
        member.setProfileImg(relativePath);
        memberRepository.save(member);
    }

    @Transactional
    public void deleteProfileImage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if (member.getProfileImg() != null) {
            try {
                Files.deleteIfExists(Paths.get(member.getProfileImg()));
            } catch (IOException e) {
                throw new RuntimeException("이미지 삭제 실패", e);
            }
            member.setProfileImg(null);
            memberRepository.save(member);
        }
    }

    @Transactional
    public void withdrawMember(Member member, MemberWithdrawalDto dto) {
        if (dto != null && !member.getEmail().equals(dto.getEmail())) {
            throw new IllegalArgumentException("이메일이 일치하지 않습니다.");
        }
        member.setState(false);
        member.setActive(false);
        memberRepository.save(member);
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }

}
