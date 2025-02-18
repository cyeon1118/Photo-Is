package org.ssafy.d103.communities.dto.question.response;

import lombok.Builder;
import lombok.Getter;
import org.ssafy.d103.communities.entity.question.QuestionComment;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DeleteQuestionCommentResponse {

    private Integer commentCnt;
    private List<QuestionCommentDto> commentList;

    @Builder
    private DeleteQuestionCommentResponse(Integer commentCnt, List<QuestionCommentDto> commentList) {
        this.commentCnt = commentCnt;
        this.commentList = commentList;
    }

    public static DeleteQuestionCommentResponse of(Integer commentCnt, List<QuestionComment> commentList) {
        List<QuestionCommentDto> responseList = commentList.stream()
                .map(QuestionCommentDto::from)
                .collect(Collectors.toList());
        return builder()
                .commentCnt(commentCnt)
                .commentList(responseList)
                .build();
    }

}