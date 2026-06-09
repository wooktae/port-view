# Decision Matrix — 02-aws-network-and-rds

본 문서는 운영자가 결정해야 하는 핵심 의사결정을 표 단위로 정리한다. 새 환경 모델(local-dev / aws-paper / aws-live)과 NAT-free 기본안을 반영해 갱신했다. 모든 비용은 서울 ap-northeast-2 기준 근사치이고 "AWS Pricing Calculator 확인 필요" 단서가 붙는다. 모든 secret은 `[REDACTED]`로만 표기한다.

세부 단가의 출처는 루트 공통 문서 [`../_common/cost-simulation.md`](../_common/cost-simulation.md).

## 1. 환경 모델

| 환경 | 위치 | 역할 | 자동 BUY/SELL | 비고 |
|------|------|------|---------------|------|
| local-dev | 운영자 로컬 PostgreSQL | 개발 / 단위 검증 / 소스 수정 | 금지 | AWS 리소스 없음. 비용 0 |
| aws-paper | AWS 단일 VPC | 1차 AWS 구축, KIS 모의투자 리허설 | 초기 차단 → 검증 후 단계적 허용 | 운영 안정성 기준 적용 |
| aws-live | AWS 단일 VPC | paper 검증 후 후속 구축, 실계좌 | 초기 자동주문 금지, 후보 + 수동 승인 | 자동 재시도 금지 정책 |

권고: 본 spec 1차 적용 환경은 aws-paper. aws-live cutover는 `10-cutover-and-validation-runbook`에서 통합 진행.

## 2. 단일 VPC vs prod / non-prod 분리

| 옵션 | 비용 | 운영 부담 | 권고 |
|------|------|-----------|------|
| 단일 VPC + 환경 SG/Subnet 태그 | 낮음 | 낮음 | 1순위(현 결정) |
| prod / non-prod VPC 분리 | NAT/Endpoint 중복 + peering 부담 | 높음 | 2순위. 규제 / 감사 요구 시 재검토 |

## 3. NAT 옵션 (참고용 비교)

본 spec은 aws-paper / aws-live 모두 NAT 미사용을 기본안으로 채택했다. 본 표는 변경 검토용 참고 자료다.

| 옵션 | 설명 | 월 고정비 (단일 AZ) | 보안 | 운영 난이도 | 본 spec 채택 |
|------|------|---------------------|------|-------------|--------------|
| A. NAT GW 1개 | 단일 AZ NAT | ~$43 + 데이터 처리 | 보통 | 낮음 | 미채택 |
| B. NAT GW 2개 (multi-AZ) | AZ별 NAT | ~$86 + 데이터 처리 | 강함 | 낮음 | 미채택 |
| C. NAT Instance `t4g.nano` | 자체 EC2 NAT | ~$3.5 + EBS + 데이터 | 약함 (SPOF) | 높음 | 보조(필요 시) |
| D. NAT 미사용 + Fargate public + EC2 public | NAT 없이 인터넷 outbound 워크로드만 public 배치 | ~$0 (NAT) | 중 (SG 검증 필수) | 낮음~중 | **1순위 채택** |

NAT-free 채택 시 절감 효과(aws-paper 기준): NAT GW 1개 ~$43/월 + 데이터 처리 ~$2~$10/월 절감.

## 4. 인터넷 outbound 워크로드 배치 옵션 (NAT-free 전제)

| 옵션 | 적용 워크로드 | 비용 영향 | 보안 리스크 | SG 통제 기준 | 운영 난이도 |
|------|---------------|-----------|------------|--------------|-------------|
| OPT-1: ECS Fargate + public subnet + assignPublicIp | crawler / preprocessor / research | NAT 없음. IPv4 사용 시간당 소액 | Task SG 실수 시 외부 inbound 가능 | inbound 0.0.0.0/0 절대 미허용. outbound는 KRX/Naver/yfinance/holiday 등 필요 도메인만 | 낮음 |
| OPT-2: marketconnector EC2 + EIP | marketconnector | EC2 단가만, EIP attach 무료 | EC2 SG 잘못 시 외부 노출 | inbound는 SSM만, port 22/외부 포트 차단 | 중 |
| OPT-3: crawler 전용 EC2 또는 ECS on EC2 | KRX 로그인 / Selenium 안정성 부족 시 crawler 승격 | EC2 24/7 비용 | EC2 직접 운영 부담 | OPT-2와 동일 | 중 |
| OPT-4: NAT Instance(`t4g.nano`) | NAT-free가 어려운 일부 워크로드 보조 | EC2 ~$3.5 + EBS + 데이터 | SPOF, multi-AZ HA 직접 구현 | NAT EC2 SG는 outbound만, inbound는 VPC 내부만 | 높음 |

권고

- 마켓커넥터: OPT-2 1순위.
- crawler / preprocessor / research: OPT-1 1순위. KRX 로그인 / Selenium 안정성 미달 시 crawler만 OPT-3 승격.
- OPT-4는 보조(필요 시).

## 5. RDS 인스턴스 / multi-AZ

| 환경 | 인스턴스 | AZ | storage | 월 RDS 비용 (근사) | 비고 |
|------|---------|----|---------|--------------------|------|
| local-dev | 해당 없음 | 해당 없음 | 해당 없음 | $0 | 기존 로컬 PostgreSQL 유지 |
| aws-paper | db.t4g.small | single-AZ | gp3 50 GB | ~$26 + storage ~$7 | retention 7일, PITR on |
| aws-live (비용 절감안) | db.t4g.medium | single-AZ | gp3 100 GB | ~$52 + storage ~$13 | AZ 장애 시 다운타임. PITR로만 복구 |
| aws-live (안정성 우선안) | db.t4g.medium | multi-AZ | gp3 100 GB | ~$104 + storage ~$13 | 자동 failover |
| aws-live (성능 우선안) | db.m6g.large | multi-AZ | gp3 200 GB | ~$330 + storage ~$26 | 트랜잭션 부하 여유 |

권고: aws-paper는 `db.t4g.small` single-AZ 시작. aws-live는 비용 절감안 single-AZ로 시작 가능, 운영 안정성 우선안은 multi-AZ.

## 6. VPC Endpoint 활성 조합 (NAT-free 핵심)

| 조합 | 활성 endpoint | 월 비용 (multi-AZ 기준) | 효과 |
|------|---------------|------------------------|------|
| 최소 | S3(Gateway 무료) + ECR(api+dkr) | ~$32 | 컨테이너 pull 안정 |
| 권고 | 위 + Secrets + SSM + Logs | ~$80 | NAT-free에서 AWS 서비스 접근 안정 |
| 확장 | 위 + KMS + STS + EC2 | ~$112 | CMK / OIDC role assume / 일부 SDK |

권고

- aws-paper: 권고 조합 + 단일 AZ로 시작 가능(Endpoint 5종 × 1 AZ ≈ ~$40/월).
- aws-live: 권고 조합 multi-AZ(~$80/월). CMK 도입 시 KMS endpoint 추가.

NAT-free 효과: NAT GW 미사용 절감 ~$43~$86/월 vs Endpoint 비용 ~$40~$80/월. net 효과는 절감 방향이며 Endpoint 가용성 / 성능 이점이 추가.

## 7. ALB 사용 여부

| 옵션 | 월 고정비 | 보안 | 운영 난이도 | 권고 환경 |
|------|---------|------|-------------|----------|
| internal ALB 1개 | ~$16.5 + LCU | 강함(VPC 내부) | 낮음 | aws-live 안정성 우선안 |
| ALB 미사용 + ECS Service Discovery + SSM 포트포워딩 | $0 | 강함 | 중 (운영자 접근 절차 필요) | aws-paper 초기, aws-live 비용 절감안 |
| public ALB + 인증 게이트 | ~$16.5 + 인증 비용 | 인증 게이트 의존 | 중상 | 운영자 외부 접속이 꼭 필요할 때 |

권고: aws-paper 초기 미사용. aws-live 초기 비용 절감안 보류. 운영자 결정에 따라 internal ALB로 승격.

## 8. Backup retention / PITR

| 환경 | retention | PITR | 월 backup storage 추가 비용 |
|------|----------|------|-----------------------------|
| local-dev | 운영자 자체 백업 | 해당 없음 | 0 |
| aws-paper | 7일 | on | 작음 |
| aws-live (비용 절감안) | 7일 | on | 작음 |
| aws-live (안정성 우선안) | 14일 | on | 중 (DB 크기 × 약 1배 무료, 초과분 ~$0.105/GB-월) |

권고: aws-live 안정성 우선안에서는 14일 + manual snapshot(분기, cutover 직전).

## 9. SG 정책

| 항목 | 옵션 | 권고 |
|------|------|------|
| RDS inbound | 0.0.0.0/0 vs SG 참조만 | SG 참조만 |
| EC2 SSH 22 | 0.0.0.0/0 vs SSM | SSM |
| public subnet ECS Task inbound | 미허용 vs allowlist | 미허용(필요 시 운영자 allowlist) |
| ALB 공개 범위 | public vs internal | internal (도입 시) |

권고: 모든 환경 동일 정책. 비용 차이 없음, 보안 차이 큼.

## 10. 데이터 cutover 방식

| 옵션 | 절차 | 다운타임 | 운영 난이도 | 권고 |
|------|------|---------|-------------|------|
| pg_dump + pg_restore | 정지 → dump → restore → 검증 → 가동 | 수십 분 | 낮음 | 1순위 |
| AWS DMS | source 연결 + ongoing replication | 수 분 | 중상 | 보류. 무중단이 꼭 필요해질 때 재검토 |
| 백업 snapshot 복원 | RDS source 필요 | 해당 없음 | 해당 없음 | 본 spec 비대상 |

권고: 1순위 `pg_dump` + `pg_restore`. cutover window 운영자 결정.

## 11. 비용 프로파일 (aws-paper)

aws-paper 환경별 월 합계 추정. 세부 단가는 루트 공통 문서 [`../_common/cost-simulation.md`](../_common/cost-simulation.md).

| 항목 | low | realistic | stable |
|------|-----|-----------|--------|
| 마켓커넥터 EC2 (`t4g.small` 24/7) + EIP attach | ~$15 | ~$15 | ~$17 |
| 인터넷 outbound ECS Fargate (crawler / preprocessor 합산, 짧은 batch) | ~$5 | ~$8 | ~$12 |
| 내부 ECS Fargate (port-view / decision / execution / research) | ~$30 | ~$45 | ~$70 |
| RDS db.t4g.small single-AZ + gp3 50GB | ~$33 | ~$33 | ~$36 |
| RDS backup retention 7일 | ~$2 | ~$3 | ~$5 |
| NAT Gateway | 0 | 0 | 0 |
| ALB | 0 | 0 | ~$17 (도입 시) |
| VPC Endpoint (단일 AZ 권고 5종) | ~$24 | ~$32 | ~$40 |
| CloudWatch Logs (5~15 GB, retention 7일) | ~$5 | ~$10 | ~$18 |
| CloudWatch Metrics + Alarms (paper 기준) | ~$5 | ~$8 | ~$12 |
| Secrets Manager (~10 secret 가정) | ~$4 | ~$5 | ~$6 |
| ECR / S3 / Step Functions / EventBridge / Lambda | ~$2 | ~$3 | ~$5 |
| **합계** | **약 125** | **약 162** | **약 238** |

비고

- low: NAT-free + ALB 미사용 + 내부 ECS 경량 + Endpoint 단일 AZ + Logs 7일 + Secrets 최소화.
- realistic: 본 spec 권고 그대로.
- stable: 운영 회고를 위해 ALB 도입 검토, Logs / Metrics 확장.

기존 [`../_common/cost-simulation.md`](../_common/cost-simulation.md)(이전 결정 기준 paper realistic ~$212)와 비교해 NAT 제거 ~$43~$50, dev AWS 제거 다른 환경 합산에서 추가 절감, ALB 미사용 ~$17 절감 효과로 약 $50~$80 더 낮아진다.

## 12. 비용 프로파일 (aws-live)

aws-live 환경별 월 합계 추정.

| 항목 | low (비용 절감안) | realistic | stable (안정성 우선안) |
|------|-------------------|-----------|------------------------|
| 마켓커넥터 EC2 (`t3.medium` 24/7) + EIP | ~$38 | ~$40 | ~$45 |
| 인터넷 outbound ECS Fargate | ~$10 | ~$15 | ~$22 |
| 내부 ECS Fargate (24/7 서비스 + 일일/장중 task) | ~$60 | ~$80 | ~$120 |
| RDS db.t4g.medium single-AZ + gp3 100GB | ~$65 | ~$70 | ~$80 |
| RDS db.t4g.medium multi-AZ + gp3 100GB | 0 (절감안) | 0 | ~$130 (대체) |
| RDS backup retention | ~$5 | ~$8 | ~$15 |
| NAT Gateway | 0 | 0 | 0 |
| ALB internal | 0 | 0 | ~$17 |
| VPC Endpoint (multi-AZ 5종) | ~$40 | ~$80 | ~$80 |
| CloudWatch Logs (30~80 GB, retention 14일) | ~$25 | ~$40 | ~$60 |
| CloudWatch Metrics + Alarms | ~$10 | ~$15 | ~$22 |
| Secrets Manager (~10 secret) | ~$4 | ~$5 | ~$7 |
| ECR / S3 / Step Functions / EventBridge / Lambda | ~$3 | ~$5 | ~$8 |
| **합계 (single-AZ 기준 절감안)** | **약 260** | **약 358** | n/a |
| **합계 (multi-AZ 기준 안정성 우선안)** | n/a | n/a | **약 506** |

비고

- low (비용 절감안): single-AZ RDS + Endpoint 비용 단일 AZ + ALB 미사용. AZ 장애 시 다운타임 허용 전제.
- realistic: single-AZ 유지 + Endpoint multi-AZ로 가용성 일부 보강.
- stable (안정성 우선안): multi-AZ RDS + Endpoint multi-AZ + internal ALB + 14일 backup. 자동 재시도 금지 정책은 모든 옵션에 동일 적용.

기존 [`../_common/cost-simulation.md`](../_common/cost-simulation.md)(이전 결정 기준 live realistic ~$492)와 비교해 NAT-free 절감 ~$86~$98, ALB 미사용 ~$17, dev AWS 제거 효과(별도 환경 합산)로 합계 약 $130~$170 절감 가능.

## 13. 비용 절감 vs 운영 리스크 항목별 정리

| 절감 항목 | 절감 효과 (월) | 운영 리스크 |
|-----------|----------------|-------------|
| NAT Gateway 제거 | ~$43~$98 | public subnet 워크로드 SG 실수 시 외부 노출 위험. SG 통제 필수 |
| AWS dev 제거 | dev 환경 합산 ~$60~$120 (이전 추정) | 통합 검증 부담이 aws-paper로 집중. paper 운영 안정성 강화 필요 |
| ALB 미사용 | ~$17 | 운영자 접근에 SSM 포트포워딩 또는 공유 IP allowlist 필요 |
| RDS single-AZ 시작 | ~$26~$50 (인스턴스 클래스에 따라) | AZ 장애 시 다운타임. PITR 의존 |
| CloudWatch Logs retention 7~14일 | ~$5~$30 | 장애 회고 / 감사 회고 부족. 회고 필요 시 임시 export 필요 |
| Secrets 항목 최소화 또는 SSM SecureString 활용 | secret 수에 비례, ~$1~$5 | 06에서 보안 / rotation 결정에 따라 변동 |
| VPC Endpoint 단일 AZ 시작 | ~$40 (multi-AZ 대비) | 1 AZ 장애 시 Endpoint 장애. application 측 retry 필요 |

## 14. 운영자 결정 체크리스트

본 체크리스트의 결정값은 루트 공통 문서 [`../_common/operator-decisions.md`](../_common/operator-decisions.md)에 기록.

- [ ] CIDR / AZ 결정
- [ ] 인터넷 outbound 워크로드 옵션 매핑(OPT-1 ~ OPT-4)
- [ ] aws-paper RDS 인스턴스 클래스
- [ ] aws-live RDS 인스턴스 / single-AZ vs multi-AZ
- [ ] VPC Endpoint 활성 항목과 AZ 정책(단일 AZ vs multi-AZ)
- [ ] ALB 도입 여부(aws-paper / aws-live)
- [ ] CloudWatch Logs retention
- [ ] backup retention(aws-paper / aws-live)
- [ ] cutover 일정 / 다운타임 허용폭
- [ ] 7개 role 비밀번호 Secrets Manager 등록 시점(spec 06과 동기)

본 체크리스트 통과 후 `tasks.md` Phase 1부터 운영자가 진행한다.

## MS별 AWS 서비스 후보 비교 참조

본 decision-matrix는 NAT / RDS / VPC Endpoint / ALB / backup / cutover 같은 인프라 옵션 결정에 집중한다. 각 MS별 컴퓨트 / orchestration 후보 비교(EC2 / ECS Fargate / ECS on EC2 / AWS Batch / Lambda / EKS / Elastic Beanstalk / App Runner / Step Functions / EventBridge Scheduler 등)와 포트폴리오 어필 관점 보강안은 루트 공통 문서 [ms-aws-service-decision-matrix.md](../_common/ms-aws-service-decision-matrix.md)에 별도로 정리되어 있다.

8개 MS 컴퓨트 1순위 결정(OD-MS-001 ~ OD-MS-010)은 루트 공통 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 9장에 락 상태로 기록되어 있다. 본 decision-matrix는 그 결정을 변경하지 않고, 네트워크 / RDS 옵션 선택에만 집중한다.

## 15. 본 spec 작업 안전 제약

- 실제 AWS 리소스 생성 금지
- 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 금지
- 실제 secret 값 출력 금지(모두 `[REDACTED]`)
- 06-secrets-and-iam은 본 작업 시점에 작성하지 않는다
