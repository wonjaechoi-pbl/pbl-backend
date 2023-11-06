package com.gogofnd.kb.domain.rider.entity;

import com.gogofnd.kb.domain.insurance.dto.req.KbApi1Req;
import com.gogofnd.kb.domain.seller.entity.Seller;
import com.gogofnd.kb.global.domain.BaseTimeEntity;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
@Table(name = "rider")
public class Rider extends BaseTimeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 운영사
    @ManyToOne(fetch = FetchType.LAZY)
    private Seller seller;

    @Column(length = 20)
    private String driver_id; // kb에 보낼 driverId

    @Column(length = 30)
    private String loginId; // 가입 요청시 운영사한테 받는 , 기사가 쓰는 로그인용 아이디

    @Column(length = 20)
    private String name;
    @Column(length = 20)
    private String phone;

    @Column(length = 8)
    private String birthDate;

    // 아마 사용안할듯??
    private String password;

    private int gender;

    @Column(length = 20)
    private String policy_number;

    // 안쓸것같음. 앱 개발 자체적으로 할 때 만든 컬럼임 ,지워도 될 것같은데 에러날까봐 무쪄워용
    @Column(length = 1)
    private Integer status; // 1 적용, 0 미적용
    @ElementCollection(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rider_roles",
            joinColumns = @JoinColumn(name="driver_id")
    )
    @Builder.Default
    private final List<String> roles = new ArrayList<>();

    private String ssn; // 주민등록번호

    @Column(name = "vcno_hngl_nm",length = 20)
    private String vcNumber; // 차량변호 한글명. 가입심사 할 때 실제 본인 소유 이륜차 아니면 심사 승인 안됨

    @Column(length = 10)
    private String region; //활동지역 송부 라이더가 활동하는 지역
    @Column(length = 10)
    private String insuranceStatus;

    @Column(length = 20)
    private String applicationNumber;

    // 라이더 보험 증서 사진 경로
    private String imagePath;

    // 라이더 별로 돈관리를 하려고 했는데 정책이 바뀌면서 운영사 기준으로 돈관리를 하게 되서 사용안함. 지워도 되는지 모르겠음;;
    private int balance;

    //카카오톡 웹뷰 유알엘
    private String totalWebViewUrl;

    //보험 적용여부 enum
    @Enumerated(EnumType.STRING)
    private ApplyStatus applyStatus;

    // 보험 가입 적용 기간 시작
    private LocalDateTime effectiveStartDate;

    //보험 가입 적용 기간 끝
    private LocalDateTime effectiveEndDate;

    // 의무보험 운행용도 코드
    private String oprn_purp;

    // 의무보험 만기일자
    @Column(length = 8)
    private String mtdt;

    private String memo;

    @Column(name = "memo_writer",length = 10)
    private String memoWriter;

    @Column(name = "use_yn",length = 1)
    private String useYn;

    @Column(name = "pay_status",length = 1)
    private String payStatus;

    public void updateStatus(Integer status){
        this.status = status;
    }

    public void updateEffectiveDate(LocalDateTime effectiveStartDate,LocalDateTime effectiveEndDate){
        this.effectiveEndDate = effectiveEndDate;
        this.effectiveStartDate = effectiveStartDate;
    }

    public void updateInsuranceStatus(String status){
        this.insuranceStatus = status;
    }

    public void updateUseYnStatus(String useYn){
        this.useYn = useYn;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
    @Override
    public String getPassword() {
        return this.password;
    }


    @Override
    public String getUsername() {
        return this.name;
    }

    public String getLoginId() { return this.loginId; }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static Rider create(KbApi1Req req, Seller seller, String rawSsn){
        //아이디 중복 방지
        String id = req.getDriver_id();
        String setMtdt = "";
        String setOprnPurp = "";
        String setDriverId = "";


        //운송용도, 보험만료일 못받아오는 경우 이렇게 처리
        if(req.getMtdt() !=null)
            setMtdt = req.getMtdt();
        if(req.getOprn_purp() !=null)
            setOprnPurp = req.getOprn_purp();

        // 드라이버 아이디 없으면 기본형식으로 저장
        if(req.getDriver_id() != null) {
            setDriverId = req.getDriver_id();
        }else{
            setDriverId = seller.getSeller_UID()+req.getDriver_phone().substring(3);
        }

        return Rider.builder()
                .seller(seller)
                .balance(0)
                .policy_number(seller.getPolicy_number())
                .applicationNumber(seller.getApplication_number())
                .insuranceStatus("")
                .birthDate(createBirth(rawSsn))
                .loginId(setDriverId)
                .gender(req.getDriver_gender())
                .region(req.getDriver_region())
                .vcNumber(req.getDriver_vcnum())
                .phone(req.getDriver_phone())
                .ssn(req.getDriver_ssn())
                .name(req.getDriver_name())
                .applyStatus(ApplyStatus.END)
                .roles(List.of("ROLE_USER"))
                .status(1) // 1 활동, 0 탈퇴
                .mtdt(setMtdt)
                .oprn_purp(setOprnPurp)
                .useYn("Y")
                .payStatus("Y")
                .build();
    }

    public void dischargeBalance(long balance) {
        this.balance -= balance;
    }

    //calls end 두번 들어갔을 때, 이전 balance 환불
    public void refundBalance(long balance) {
        this.balance += balance;
    }

    public void updateApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public void withdraw() {
        this.status = 0;
    }

    public void updateImage(String imagePath) {
        this.imagePath = imagePath;
    }

    public void insuranceApply() {
        this.applyStatus = ApplyStatus.APPLY;
    }

    public void insuranceEnd() {
        this.applyStatus = ApplyStatus.END;
    }

    public void updateTotalWebViewUrl(String totalWebViewUrl) {
        this.totalWebViewUrl = totalWebViewUrl;
    }

    public void createDriverId(String leftPadDriverId) {
        this.driver_id = leftPadDriverId;
    }

    private static String createBirth(String ssn) {
        String ssnBirth = ssn.substring(0, 7);
        String lastNum = ssnBirth.substring(6);

        String birthDate;

        if (lastNum.equals("1") || lastNum.equals("2") || lastNum.equals("5") || lastNum.equals("6")) {
            birthDate = "19";
        } else if (lastNum.equals("3") || lastNum.equals("4") || lastNum.equals("7") || lastNum.equals("8")) {
            birthDate = "20";
        } else {
            // 주민번호가 비정상적일 경우
            birthDate = "99";
        }

        return birthDate + ssnBirth.substring(0, 6);
    }

    public void delete(Long riderCnt){
        String deleteComment = "삭제";
        this.name = deleteComment + this.name;
        this.phone = deleteComment + riderCnt + this.phone;
        this.vcNumber = deleteComment + this.vcNumber;
        this.driver_id = deleteComment + this.driver_id;
        this.loginId = deleteComment + riderCnt + this.loginId;
        this.insuranceStatus = deleteComment + this.insuranceStatus;
        this.useYn = "N";
        this.setDeletedDate(LocalDateTime.now());
    }
}