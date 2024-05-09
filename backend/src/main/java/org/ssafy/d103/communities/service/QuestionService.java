package org.ssafy.d103.communities.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.ssafy.d103._common.exception.CustomException;
import org.ssafy.d103._common.exception.ErrorType;
import org.ssafy.d103._common.service.CommonService;
import org.ssafy.d103.communities.dto.question.PaginationDataDto;
import org.ssafy.d103.communities.dto.question.QuestionDto;
import org.ssafy.d103.communities.dto.question.request.PostUploadQuestionRequest;
import org.ssafy.d103.communities.dto.question.response.GetQuestionDetailResponse;
import org.ssafy.d103.communities.dto.question.response.GetQuestionListResponse;
import org.ssafy.d103.communities.dto.question.response.PostUploadQuestionResponse;
import org.ssafy.d103.communities.entity.photo.Photo;
import org.ssafy.d103.communities.entity.question.Category;
import org.ssafy.d103.communities.entity.question.Question;
import org.ssafy.d103.communities.entity.question.QuestionDetail;
import org.ssafy.d103.communities.repository.photo.PhotoRepository;
import org.ssafy.d103.communities.repository.question.QuestionDetailRepository;
import org.ssafy.d103.communities.repository.question.QuestionRepository;
import org.ssafy.d103.members.entity.Members;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    private final QuestionDetailRepository questionDetailRepository;

    private final PhotoRepository photoRepository;

    private final CommonService commonService;

    public PostUploadQuestionResponse uploadQuestion(Authentication authentication, PostUploadQuestionRequest postUploadQuestionRequest) {
        Members member = commonService.findMemberByAuthentication(authentication);

        // 질문에 사진을 포함한 경우
        if (postUploadQuestionRequest.getPhotoId() != null) {
            Photo photo = photoRepository.findPhotoByIdAndMember(postUploadQuestionRequest.getPhotoId(), member)
                    .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND_PHOTO));

            try {
                Question question = questionRepository.save(Question.of(postUploadQuestionRequest.getTitle(), postUploadQuestionRequest.getContent(),
                        Category.fromString(postUploadQuestionRequest.getCategory()), member, photo));

                questionDetailRepository.save(QuestionDetail.init(question));

                return PostUploadQuestionResponse.of(true);
            } catch (Exception e) {
                e.getStackTrace();
                throw new CustomException(ErrorType.DB_SAVE_ERROR);
            }
        }
        // 질문에 사진을 포함하지 않은 경우
        else {
            try {
                Question question = questionRepository.save(Question.of(postUploadQuestionRequest.getTitle(), postUploadQuestionRequest.getContent(),
                        Category.fromString(postUploadQuestionRequest.getCategory()), member));

                questionDetailRepository.save(QuestionDetail.init(question));

                return PostUploadQuestionResponse.of(true);
            } catch (Exception e) {
                e.getStackTrace();
                throw new CustomException(ErrorType.DB_SAVE_ERROR);
            }
        }
    }

    public GetQuestionListResponse getQuestionList(int page, int size) {
        // 페이지를 1부터 시작하도록 0으로 설정
        // 음수 페이지가 요청되지 않도록 설정
        int adjustedPage = Math.max(page - 1, 0);

        // 페이지네이션 요청을 (기본 10 size)최신순으로 생성
        Pageable pageable = PageRequest.of(adjustedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 페이지네이션된 질문 데이터를 가져옴
        Page<Question> questionPage = questionRepository.findAll(pageable);

        // 페이지에 데이터가 없을 경우 예외 발생
        if (questionPage.isEmpty()) {
            throw new CustomException(ErrorType.NOT_FOUND_QUESTION_PAGE);
        }

        // 각 질문을 QuestionResponse로 변환
        List<QuestionDto> questions = questionPage.getContent().stream()
                .map(q -> QuestionDto.of(
                        q.getId(),
                        q.getMember().getId(),
                        q.getMember().getNickname(),
                        q.getCategory().getCategoryName(),
                        q.getTitle(),
                        q.getPhoto() != null,
                        q.getQuestionDetail().getCommentCnt(),
                        q.getQuestionDetail().getViewCnt(),
                        q.getCreatedAt()))
                .collect(Collectors.toList());

        // 데이터와 페이지네이션 정보를 구성
        PaginationDataDto paginationDataDto = PaginationDataDto.of(
                questionPage.getNumber() + 1,
                questionPage.getTotalPages(),
                (int) questionPage.getTotalElements(),
                questionPage.getSize());

        // 전체 질문 수 및 질문 리스트 응답 생성
        return GetQuestionListResponse.of((int) questionPage.getTotalElements(), questions, paginationDataDto);
    }

    public GetQuestionDetailResponse getQuestionDetail(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND_QUESTION));

        return GetQuestionDetailResponse.from(question);
    }

}