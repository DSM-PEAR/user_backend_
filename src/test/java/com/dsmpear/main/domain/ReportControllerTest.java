package com.dsmpear.main.domain;

import com.dsmpear.main.MainApplication;
import com.dsmpear.main.config.ObjectMapperConfiguration;
import com.dsmpear.main.entity.comment.Comment;
import com.dsmpear.main.entity.comment.CommentRepository;
import com.dsmpear.main.entity.member.Member;
import com.dsmpear.main.entity.member.MemberRepository;
import com.dsmpear.main.entity.report.*;
import com.dsmpear.main.entity.user.User;
import com.dsmpear.main.entity.user.UserRepository;
import com.dsmpear.main.entity.userreport.UserReport;
import com.dsmpear.main.entity.userreport.UserReportRepository;
import com.dsmpear.main.payload.request.CommentRequest;
import com.dsmpear.main.payload.request.ReportRequest;
import com.dsmpear.main.payload.response.ReportContentResponse;
import com.dsmpear.main.payload.response.ReportListResponse;
import com.dsmpear.main.payload.response.ReportResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class)
@ActiveProfiles("test")
public class ReportControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private ObjectMapperConfiguration objectMapperConfiguration;

    private MockMvc mvc;

    @BeforeEach
    public void setUp() {

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        userRepository.save(
                User.builder()
                        .email("test@dsm.hs.kr")
                        .name("홍길동")
                        .password(passwordEncoder.encode("1234"))
                        .authStatus(true)
                        .build()
        );

        userRepository.save(
                User.builder()
                        .email("test1@dsm.hs.kr")
                        .name("고길동")
                        .password(passwordEncoder.encode("1234"))
                        .authStatus(true)
                        .build()
        );


    }

    @AfterEach
    public void after() {
        memberRepository.deleteAll();
        reportRepository.deleteAll();
        userRepository.deleteAll();
        commentRepository.deleteAll();
        userReportRepository.deleteAll();
    }

    // 보고서 작성 성공 테스트
    @Test
    @WithMockUser(value = "test@dsm.hs.kr",password="1234")
    public void createReportTest() throws Exception {

        ReportRequest request = ReportRequest.builder()
                .title("1. 이승윤 돼지")
                .description("내애용은 이승윤 돼지")
                .grade(Grade.GRADE2)
                .access(Access.EVERY)
                .field(Field.AI)
                .type(Type.TEAM)
                .isSubmitted(false)
                .github("깃허브으")
                .languages("자바")
                .fileName("이승윤 돼지")
                .teamName("이름")
                .build();

        String requests = objectMapperConfiguration.objectMapper().writeValueAsString(request);


        mvc.perform(post("/report")
                .content(requests)
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().is2xxSuccessful()).andDo(print());

    }

    // 보고서 작성 실패 테스트(UserNotFound)
    @Test
    public void createReportTest2() throws Exception {

        ReportRequest request = ReportRequest.builder()
                .title("1. 이승윤 돼지")
                .description("내애용은 이승윤 돼지")
                .grade(Grade.GRADE2)
                .access(Access.EVERY)
                .field(Field.AI)
                .type(Type.TEAM)
                .isSubmitted(false)
                .github("깃허브으")
                .languages("자바")
                .fileName("이승윤 돼지")
                .teamName("dfas")
                .build();

        mvc.perform(post("/report")
                .content(objectMapperConfiguration.objectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().isNotFound()).andDo(print());

    }

    // 보고서 작성 실패 테스트(InvalidData인데 에러메세지는 없음)
    @Test
    @WithMockUser(value = "test@dsm.hs.kr",password="1234")
    public void createReportTest3() throws Exception {

        ReportRequest request = ReportRequest.builder()
                .title("")
                .description("내애용은 이승윤 돼지")
                .grade(Grade.GRADE2)
                .access(Access.EVERY)
                .field(Field.AI)
                .type(Type.TEAM)
                .isSubmitted(false)
                .github("깃허브으")
                .languages("자바")
                .fileName("이승윤 돼지")
                .teamName("dfas")
                .build();

        mvc.perform(post("/report")
                .content(objectMapperConfiguration.objectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().is4xxClientError()).andDo(print());

    }

    // 보고서 보기 성공 테스트
    @Test
    @WithMockUser(value = "test@dsm.hs.kr",password="1234")
    public void getReportTest() throws Exception {

        String expected = "expect";

        Integer reportId1 = createReport("애는 좀 다르고");
        Integer reportId = createReport(expected);
        Integer reportId2 = createReport("이건 정상적이게 비슷");

        MvcResult mvcResult = mvc.perform(get("/report/"+reportId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().isOk()).andDo(print()).andReturn();

        ReportContentResponse response = objectMapperConfiguration.objectMapper().readValue(mvcResult.getResponse().getContentAsString(), ReportContentResponse.class);
        Assert.assertEquals(response.getTitle(), expected);
    }

    // 보고서 보기 성공 테스트(EVERY꺼)
    @Test
    @WithMockUser(value = "test1@dsm.hs.kr",password="1234")
    public void getReportTest1() throws Exception {

        String expected = "expected";

        Integer reportId = createReport(expected);


        MvcResult mvcResult = mvc.perform(get("/report/"+reportId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().isOk()).andDo(print()).andReturn();

        ReportContentResponse response = objectMapperConfiguration.objectMapper().readValue(mvcResult.getResponse().getContentAsString(), ReportContentResponse.class);
        Assert.assertEquals(response.getTitle(), expected);
    }

    @Test
    public void getReportTest2() throws Exception {

        Integer reportId = createReport("이건 정상적이게");

        mvc.perform(get("/report/"+reportId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().isForbidden()).andDo(print());
    }

    // 보고서 업데이트 성공 테스트
    @Test
    @WithMockUser(value = "test@dsm.hs.kr",password="1234")
    public void updateReportTest() throws Exception {

        Integer reportId = createReport("이건 정상적이게");


        ReportRequest request = ReportRequest.builder()
                .title("2. 이승윤 돼지")
                .description("2째 돼지 이승윤")
                .languages("돼지")
                .type(Type.TEAM)
                .access(Access.ADMIN)
                .grade(Grade.GRADE1)
                .field(Field.AI)
                .fileName("돼지")
                .isSubmitted(false)
                .github("깃허브ㅡ")
                .teamName("dfas")
                .build();

        mvc.perform(patch("/report/"+reportId)
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().is2xxSuccessful()).andDo(print());
    }

    // 보고서 업데이트 실패 테스트(userNotMemer)
    @Test
    @WithMockUser(value = "test1@dsm.hs.kr",password="1234")
    public void updateReportTest1() throws Exception {

        Integer reportId = createReport("이건 정상적이게");


        ReportRequest request = ReportRequest.builder()
                .title("2. 이승윤 돼지")
                .description("2째 돼지 이승윤")
                .languages("돼지")
                .type(Type.TEAM)
                .access(Access.ADMIN)
                .grade(Grade.GRADE1)
                .field(Field.AI)
                .fileName("돼지")
                .isSubmitted(false)
                .github("깃허브ㅡ")
                .teamName("dfas")
                .build();

        mvc.perform(patch("/report/"+reportId)
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().is4xxClientError()).andDo(print());
    }

    // 보고서 업데이트 실패 테스트(UserNotFound)
    @Test
    public void updateReportTest2() throws Exception {

        Integer reportId = createReport("이건 정상적이게");


        ReportRequest request = ReportRequest.builder()
                .title("2. 이승윤 돼지")
                .description("2째 돼지 이승윤")
                .languages("돼지")
                .type(Type.TEAM)
                .access(Access.ADMIN)
                .grade(Grade.GRADE1)
                .field(Field.AI)
                .fileName("돼지")
                .isSubmitted(false)
                .github("깃허브ㅡ")
                .teamName("dfas")
                .build();


        mvc.perform(patch("/report/"+reportId)
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().is4xxClientError()).andDo(print());
    }

    // 보고서 삭제 성공 테스트
    @Test
    @WithMockUser(value = "test@dsm.hs.kr",password="1234")
    public void deleteReportTest() throws Exception {
        Integer reportId = createReport("testetsetesstest");

        mvc.perform(delete("/report/{reportId}", Integer.toString(reportId))).andDo(print())
                .andExpect(status().isOk()).andDo(print());
    }

    // 보고서 삭제 실패 테스트
    @Test
    @WithMockUser(value = "test12@dsm.hs.kr",password="1234")
    public void deleteReportTest1() throws Exception {
        Integer reportId = createReport("헑");

        mvc.perform(delete("/report/"+reportId)).andDo(print())
                .andExpect(status().isNotFound()).andDo(print());
    }

    // 보고서 삭제 실패 테스트
    @Test
    public void deleteReportTest2() throws Exception {
        Integer reportId = createReport("핡");

        mvc.perform(delete("/report/"+reportId)).andDo(print())
                .andExpect(status().isNotFound()).andDo(print());
    }

    // 댓글 작성 성공 테스트
    @Test
    @WithMockUser(value = "test@dsm.hs.kr", password = "1234")
    public void createComment() throws Exception {
        Integer reportId = createReport("흹");
        Integer commentId1 = createComment(reportId);

        CommentRequest request = CommentRequest.builder()
                .reportId(reportId)
                .userEmail("test@dsm.hs.kr")
                .content("아이야아이야")
                .build();

        mvc.perform(post("/comment")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().isOk()).andDo(print());

    }

    // 댓글 작성 실패 테스트
    @Test
    public void createComment1() throws Exception {
        Integer reportId = createReport("쭯");
        Integer commentId1 = createComment(reportId);

        CommentRequest request = CommentRequest.builder()
                .reportId(reportId)
                .userEmail("test@dsm.hs.kr")
                .content("아이야아이야")
                .build();

        mvc.perform(post("/comment")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().isForbidden()).andDo(print());

    }

    // 댓글 수정 성공 테스트
    @Test
    @WithMockUser(value = "test@dsm.hs.kr", password = "1234")
    public void updateComment() throws Exception {
        Integer reportId = createReport("똷");
        Integer commentId1 = createComment(reportId);
        Integer commentId2 = createComment(reportId);

        mvc.perform(patch("/comment/"+commentId1)
                .param("content", "content")).andDo(print())
                .andExpect(status().isOk()).andDo(print());

    }

    @Test
    @WithMockUser(value = "test22@dsm.hs.kr", password = "1234")
    public void updateComment2() throws Exception {
        Integer reportId = createReport("끫");
        Integer commentId1 = createComment(reportId);
        Integer commentId2 = createComment(reportId);

        mvc.perform(patch("/comment/"+commentId1)
                .param("content", "content")).andDo(print())
                .andExpect(status().isNotFound()).andDo(print());

    }


    // 댓글 수정 실패 테스트
    @Test
    public void updateComment1() throws Exception {
        Integer reportId = createReport("제엥ㅁ냐ㅐ럼니ㅏㅇ");
        Integer commentId1 = createComment(reportId);
        Integer commentId2 = createComment(reportId);

        mvc.perform(patch("/comment/"+commentId1)
                .param("content", "content")).andDo(print())
                .andExpect(status().isForbidden()).andDo(print());

    }

    // 댓글 삭제 성공 테스트
    @Test
    @WithMockUser(value = "test@dsm.hs.kr", password = "1234")
    public void deleteComment() throws Exception {
        Integer reportId = createReport("제에에에에에ㅔ에에목");
        Integer commentId1 = createComment(reportId);
        Integer commentId2 = createComment(reportId);

        mvc.perform(delete("/comment/{commentId}", Integer.toString(commentId1))
        ).andDo(print())
                .andExpect(status().isOk()).andDo(print());

    }


    @Test
    @WithMockUser(value = "test1@dsm.hs.kr", password = "1234")
    public void deleteComment1() throws Exception {
        Integer reportId = createReport("제에에에목");
        Integer commentId1 = createComment(reportId);
        Integer commentId2 = createComment(reportId);

        mvc.perform(delete("/comment/"+commentId1)).andDo(print())
                .andExpect(status().isForbidden()).andDo(print());

    }


    @Test
    public void deleteComment2() throws Exception {
        Integer reportId = createReport("제에목");
        Integer commentId1 = createComment(reportId);
        Integer commentId2 = createComment(reportId);

        mvc.perform(delete("/comment/"+commentId1)).andDo(print())
                .andExpect(status().isForbidden()).andDo(print());

    }

    private Integer addMember(Integer reportId) {
        return memberRepository.save(
                Member.builder()
                        .reportId(reportId)
                        .userEmail("test@dsm.hs.kr")
                        .report(reportRepository.findByReportId(reportId).get())
                        .build()
        ).getId();
    }

    // 보고서 목록 성공(필터 둘다)
    @Test
    @WithMockUser(value = "test@dsm.hs.kr",password="1234")
    public void getReportListTest1() throws Exception {

        Integer reportId = createReport("제에목");
        Integer reportId1 = createReport("제에에에목");
        Integer reportId2 = createReport("제에에에에에ㅔ에목");

        mvc.perform(get("/report/filter?field=AI&type=SOLE&grade=GRADE1")
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().is4xxClientError()).andDo(print());
    }

    // 보고서 목록(타입 없음)
    @Test
    @WithMockUser(value = "test@dsm.hs.kr",password="1234")
    public void getReportListTest2() throws Exception {

        Integer reportId = createReport("제에목");
        Integer reportId1 = createReport("제에에에목");
        Integer reportId2 = createReport("제에에에에ㅔ에에에목");

        mvc.perform(get("/report/filter?field=AI&grade=GRADE1")
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().is4xxClientError()).andDo(print());
    }


    // 보고서 목록(필드 없음)
    @Test
    @WithMockUser(value = "test@dsm.hs.kr",password="1234")
    public void getReportListTest3() throws Exception {

        Integer reportId = createReport("제에목");
        Integer reportId1 = createReport("제에엥목");
        Integer reportId2 = createReport("제에에에ㅔ에에목");

        mvc.perform(get("/report/filter?grade=GRADE1&type=TEAM")
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().is4xxClientError()).andDo(print());
    }

    // 보고서 목록 실패(학년 없음)
    @Test
    @WithMockUser(value = "test@dsm.hs.kr",password="1234")
    public void getReportListTest4() throws Exception {

        Integer reportId = createReport("제에목");
        Integer reportId1 = createReport("제에에에ㅔ에목");
        Integer reportId2 = createReport("제에에에에ㅔ에에목");

        mvc.perform(get("/report/filter?type=TEAM&field=AI")
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(status().is4xxClientError()).andDo(print());
    }

    private Integer createReport(String title) throws Exception {

        Report report = reportRepository.save(
                 Report.builder()
                        .title(title)
                        .description("이승윤 돼애애애지")
                        .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                        .grade(Grade.GRADE1)
                        .access(Access.ADMIN)
                        .field(Field.AI)
                        .type(Type.SOLE)
                        .isAccepted(false)
                        .isSubmitted(false)
                        .fileName("파아아일")
                        .github("기이이잇허브")
                        .languages("어어너ㅓㅓㅓ너ㅓ")
                        .teamName("asdf")
                        .build()
        );

        userReportRepository.save(
                UserReport.builder()
                        .userEmail("test@dsm.hs.kr")
                        .reportId(report.getReportId())
                        .build()
        );

        memberRepository.save(
                Member.builder()
                        .report(report)
                        .reportId(report.getReportId())
                        .userEmail("test@dsm.hs.kr")
                        .build()
        );
        return report.getReportId();
    }

    private Integer createComment(Integer reportId) throws Exception {
        return commentRepository.save(
                Comment.builder()
                        .reportId(reportId)
                        .userEmail("test@dsm.hs.kr")
                        .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                        .content("아이야아이야")
                        .build()
        ).getId();
    }

}