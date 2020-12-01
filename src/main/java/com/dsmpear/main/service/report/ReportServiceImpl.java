package com.dsmpear.main.service.report;

import com.dsmpear.main.entity.comment.Comment;
import com.dsmpear.main.entity.comment.CommentRepository;
import com.dsmpear.main.entity.member.MemberRepository;
import com.dsmpear.main.entity.report.Access;
import com.dsmpear.main.entity.report.Report;
import com.dsmpear.main.entity.report.ReportRepository;
import com.dsmpear.main.entity.team.Team;
import com.dsmpear.main.entity.team.TeamRepository;
import com.dsmpear.main.entity.user.User;
import com.dsmpear.main.entity.user.UserRepository;
import com.dsmpear.main.exceptions.PermissionDeniedException;
import com.dsmpear.main.exceptions.ReportNotFoundException;
import com.dsmpear.main.exceptions.TeamNotFoundException;
import com.dsmpear.main.exceptions.UserNotFoundException;
import com.dsmpear.main.payload.request.ReportRequest;
import com.dsmpear.main.payload.response.ApplicationListResponse;
import com.dsmpear.main.payload.response.ReportCommentsResponse;
import com.dsmpear.main.payload.response.ReportContentResponse;
import com.dsmpear.main.security.auth.AuthenticationFacade;
import com.dsmpear.main.service.comment.CommentService;
import com.dsmpear.main.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authenticationFacade;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;
    private final TeamService teamService;

    // 보고서 작성
    @Override
    public void writeReport(ReportRequest reportRequest) {
        User user = userRepository.findByEmail(authenticationFacade.getUserEmail())
                .orElseThrow(UserNotFoundException::new);
        reportRepository.save(
                Report.builder()
                        .title(reportRequest.getTitle())
                        .description(reportRequest.getDescription())
                        .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                        .grade(reportRequest.getGrade())
                        .access(reportRequest.getAccess())
                        .field(reportRequest.getField())
                        .type(reportRequest.getType())
                        .isAccepted(0)
                        .languages(reportRequest.getLanguages())
                        .fileName(reportRequest.getFileName())
                        .build()
        );
    }

    // 보고서 보기
    @Override
    public ReportContentResponse viewReport(Integer reportId) {
        boolean isMine = false;
        // 이메일 받아오기. 없으면 null이 들어갈껄?
        String email = authenticationFacade.getUserEmail();

        // 보고서를 아이디로 찾기
        Report report = reportRepository.findByReportId(reportId)
                .orElseThrow(ReportNotFoundException::new);

        // 보고서의 팀을 받아오자ㅏㅏ
        Team team = teamRepository.findByReportId(reportId)
                .orElseThrow(TeamNotFoundException::new);

        if(email != null) {
            isMine = !memberRepository.findByTeamIdAndUserEmail(team.getId(), email).isEmpty();

            // 보고서를 볼 때 보는 보고서의 access가 ADMIN인지, 만약 admin이라면  현재 유저가 글쓴이가 맞는지 검사
            if (report.getAccess().equals(Access.ADMIN) && !isMine) {
                throw new PermissionDeniedException();
            }
        }else {
            if(report.getAccess().equals(Access.USER)) {
                throw new PermissionDeniedException();
            }
        }

        List<Comment> comment = commentRepository.findAllByReportIdOrderByIdAsc(reportId);
        List<ReportCommentsResponse> commentsResponses = new ArrayList<>();


        // 댓글 하나하나 담기ㅣ
        for (Comment co : comment) {
            User commentWriter;
            if(authenticationFacade.getUserEmail() != null) {
                commentWriter = userRepository.findByEmail(co.getUserEmail())
                        .orElseThrow(UserNotFoundException::new);
            }else {
                throw new PermissionDeniedException();
            }
            commentsResponses.add(
                    ReportCommentsResponse.builder()
                            .content(co.getContent())
                            .createdAt(co.getCreatedAt())
                            .userEmail(co.getUserEmail())
                            .isMine(commentWriter.getEmail().equals(authenticationFacade.getUserEmail()))
                            .build()
            );
        }

        return ReportContentResponse.builder()
                .access(report.getAccess())
                .grade(report.getGrade())
                .type(report.getType())
                .languages(report.getLanguages())
                .title(report.getTitle())
                .fileName(report.getFileName())
                .createdAt(report.getCreatedAt())
                .description(report.getDescription())
                .isMine(isMine)
                .comments(commentsResponses)
                .build();
    }

    public Integer updateReport(Integer boardId, ReportRequest reportRequest) {
        User user = userRepository.findByEmail(authenticationFacade.getUserEmail())
                .orElseThrow(UserNotFoundException::new);

        Report report = reportRepository.findByReportId(boardId).
                orElseThrow(ReportNotFoundException::new);

        reportRepository.save(report.update(reportRequest));

        return boardId;
    }

    public void deleteReport(Integer reportId) {
        User user = userRepository.findByEmail(authenticationFacade.getUserEmail())
                .orElseThrow(UserNotFoundException::new);

        Report report = reportRepository.findByReportId(reportId)
                .orElseThrow(ReportNotFoundException::new);

        Team team = teamRepository.findByReportId(report.getReportId())
                .orElseThrow(TeamNotFoundException::new);

        System.out.println(team.getReportId());

        memberRepository.findByTeamIdAndUserEmail(team.getId(),user.getEmail())
                .orElseThrow(PermissionDeniedException::new);

        for(Comment comment : commentRepository.findAllByReportIdOrderByIdAsc(reportId)) {
            commentService.deleteComment(comment.getId());
        }

        teamRepository.deleteById(team.getId());

        reportRepository.deleteById(reportId);
    }

    @Override
    public ApplicationListResponse getReportList(Pageable page) {
        return searchReport(page,"title","");
    }

    @Override
    public ApplicationListResponse searchReport(Pageable page, String mode, String query) {
    /* 공사중
    용성짱이 알려줄 예정


    boolean isLogined= authenticationFacade.getUserEmail() == null;
        ApplicationListResponse a = null;
        page = PageRequest.of(Math.max(0, page.getPageNumber()-1), page.getPageSize());
        Page<Report> reportPage;
        switch(mode) {
            case "title":
                reportPage = reportRepository
                        .findAllByTitleContainsOrderByCreatedAt(page,query);
                break;
            case "languages":
                reportPage = reportRepository
                        .findAllByLanguagesContainsOrderByCreatedAt(page, query);
                break;
            default:
                break;
        }*/
        ApplicationListResponse a = null;
        return a;

    }

}
