# AWS 월 비용 시뮬레이션 — PORT-STRATEGY-AI AWS Migration

## Purpose

본 문서는 PORT-STRATEGY-AI AWS Migration의 월 비용 판단 기준을 정리한다.

비용은 아래 세 가지로 구분한다.

| 항목 | 값 |
| --- | --- |
| 실측 | AWS Billing에서 확인한 실제 청구 금액 |
| 운영 예상 | 현재 aws-paper 구조와 자동화 수준을 반영한 월 예상 범위 |
| 모델링 | 향후 live 또는 고가용성 구성을 가정한 비교 시나리오 |

모든 예상 비용은 서울 `ap-northeast-2` 기준 근사치이며, 실제 구축·변경 전 AWS Pricing Calculator로 다시 확인한다.

날짜별 실행 메모, Scheduler 활성화 비용, Lambda 호출 횟수, executionName, ARN, 상세 smoke 결과는 본 문서에 누적하지 않는다.

민감정보와 계정 식별정보는 기록하지 않고 `[REDACTED]` 계열 placeholder만 사용한다.

## Cost Dashboard

### 현재 aws-paper 실측 기준

| 항목 | 값 |
| --- | --- |
| 실측 대상 | 2026년 6월 |
| 세전 비용 | 135.40 USD |
| 세금 | 13.55 USD |
| 세금 포함 | 약 148.95 USD |
| 당시 핵심 cost driver | VPC Endpoint |
| NAT Gateway | 미사용 |
| 현재 운영 예상 | 약 150~190 USD/월 |
| 보수적 예산선 | 약 180 USD/월 |
| 정확한 금액 | 다음 청구 주기와 Pricing Calculator로 재검증 |

### 2026년 6월 서비스별 실측

| 항목 | 값 |
| --- | --- |
| VPC | 74.24 USD |
| RDS | 32.57 USD |
| EC2 | 25.15 USD |
| Secrets Manager | 2.88 USD |
| ECS · S3 · ECR · Data Transfer | 합산 소액 |
| CloudWatch · Lambda · Step Functions · Scheduler | 0 USD 수준 |
| 합계 세전 | 135.40 USD |
| 합계 세금 포함 | 약 148.95 USD |

### 핵심 cost driver

| 항목 | 값 |
| --- | --- |
| 1순위 | Interface VPC Endpoint의 개수와 AZ 수 |
| 2순위 | RDS 인스턴스와 Multi-AZ 여부 |
| 3순위 | 상시 실행 EC2와 Fargate Service |
| 4순위 | NAT Gateway와 데이터 처리 |
| 5순위 | ALB와 CloudWatch Logs 사용량 |

### 현재 절감 효과

| 항목 | 값 |
| --- | --- |
| SSM Endpoint | 제거 완료 |
| ECR API Endpoint | 2 AZ → 1 AZ |
| ECR DKR Endpoint | 2 AZ → 1 AZ |
| Logs Endpoint | 2 AZ → 1 AZ |
| Secrets Manager Endpoint | 2 AZ → 1 AZ |
| 예상 절감액 | 약 56.16 USD/월 |
| 추가 Endpoint 삭제 | 작동 리스크 대비 이익이 작아 보류 |
| 검증 방법 | 다음 월 청구액과 Endpoint Hours 비교 |

## Operating Cost Models

### 운영 모드별 월 예상 비용

| 항목 | 값 |
| --- | --- |
| Archive Mode | 5~20 USD |
| DB Retained Mode | 45~70 USD |
| Private AWS API Mode | 110~150 USD |
| Paper Daily Full ON | 150~190 USD |
| Demo / Interview Mode | 170~220 USD |
| Live Trading Ready Mode | 200~280 USD |

### 환경별 모델링 범위

| 항목 | 값 |
| --- | --- |
| local-dev | 별도 AWS dev 환경 미구축 · 추가 고정비 0 |
| aws-paper 현재 구조 | 약 150~190 USD/월 |
| aws-paper NAT 포함 모델 | 약 180~300 USD/월 |
| aws-live 절감형 | 약 200~350 USD/월 |
| aws-live 고가용성형 | 약 430~650 USD/월 |
| 3환경 동시 상시 운영 | 현재 운영 원칙과 불일치 · 참고용 모델만 유지 |

### 모델링 해석

| 항목 | 값 |
| --- | --- |
| paper 212 USD | NAT Gateway와 높은 Logs·Endpoint 가정을 포함한 상한 모델 |
| live 492 USD | Multi-AZ RDS · NAT 2개 · ALB · Multi-AZ Endpoint 포함 |
| 3환경 800 USD | dev · paper · live를 동시에 상시 운영하는 비교 시나리오 |
| 현재 우선값 | 실측 148.95 USD와 paper 150~190 USD 범위 |
| 사용 원칙 | 실측과 모델링 수치를 같은 의미로 인용하지 않음 |

## Current Architecture Cost Assumptions

### Compute

| 항목 | 값 |
| --- | --- |
| MarketConnector | EC2 + EIP |
| port-view | ECS Fargate Service · 필요 시 desiredCount 1 |
| Crawler | non-GUI ECS Fargate Task · KRX GUI Windows EC2 |
| Preprocessor | ECS Fargate Task |
| Strategy Decision | ECS Fargate Task |
| Strategy Execution | ECS Fargate Task + Step Functions |
| Research | AWS Batch + S3 |
| Strategy Common | 별도 컴퓨트 없음 |

### Database

| 항목 | 값 |
| --- | --- |
| aws-paper | RDS PostgreSQL `db.t4g.small` single-AZ |
| aws-live 기본 후보 | `db.t4g.medium` |
| aws-live 고가용성 | Multi-AZ |
| paper storage | 약 50 GB 가정 |
| live storage | 약 100~200 GB 가정 |
| paper backup | 7일 |
| live backup | 14일 + PITR |

### Network

| 항목 | 값 |
| --- | --- |
| NAT Gateway | paper · live 기본 미사용 |
| Public workload | Public Subnet + 필요한 경우 Public IP |
| MarketConnector | EIP 고정 |
| RDS | Private Subnet |
| S3 Endpoint | Gateway Endpoint · 무료 |
| Interface Endpoint | 필요한 서비스만 1 AZ 우선 |
| ALB | 초기 미사용 · 정식 외부 노출 시 검토 |

### Observability and Security

| 항목 | 값 |
| --- | --- |
| CloudWatch Logs | 짧은 retention으로 시작 |
| CloudWatch Alarm | 핵심 장애와 비용 spike만 설정 |
| Secrets Manager | 고민감 secret만 보관 |
| SSM Parameter Store | 저민감 설정에 Standard tier 사용 |
| Step Functions | transition 수가 적어 현재 비용 영향 미미 |
| EventBridge Scheduler | 현재 사용량은 무료 한도 내 |
| Lambda | Dispatcher·Notifier 중심 · 무료 한도 내 |

## Unit Cost Reference

정확한 단가는 AWS Pricing Calculator에서 다시 확인한다. 아래 값은 비교용 근사치다.

### Compute

| 항목 | 값 |
| --- | --- |
| EC2 `t4g.small` | 약 15 USD/월 |
| EC2 `t3.medium` | 약 38 USD/월 |
| Fargate 0.5 vCPU + 1 GB 상시 | 약 22~23 USD/월 |
| Fargate 0.5 vCPU + 1 GB 월 30시간 | 약 1 USD 이하 |
| AWS Batch on Fargate | Fargate 사용 시간 기준 |

### Network

| 항목 | 값 |
| --- | --- |
| Public IPv4 | 약 0.005 USD/시간 |
| NAT Gateway 1개 | 약 43 USD/월 + 데이터 처리 |
| NAT Gateway 2개 | 약 86 USD/월 + 데이터 처리 |
| ALB | 약 16~17 USD/월 + LCU |
| Interface Endpoint | AZ당 약 8 USD/월 |
| S3 Gateway Endpoint | 무료 |
| 인터넷 outbound | 사용량에 따라 별도 과금 |

### Database and Storage

| 항목 | 값 |
| --- | --- |
| RDS `db.t4g.small` single-AZ | 약 26 USD/월 |
| RDS `db.t4g.medium` single-AZ | 약 52 USD/월 |
| RDS `db.t4g.medium` Multi-AZ | 약 104 USD/월 |
| RDS gp3 storage | 약 0.131 USD/GB-월 |
| S3 Standard | 약 0.025 USD/GB-월 |
| ECR storage | 약 0.10 USD/GB-월 |

### Security and Observability

| 항목 | 값 |
| --- | --- |
| Secrets Manager | 약 0.40 USD/secret-월 |
| SSM Parameter Store Standard | 무료 |
| CloudWatch Logs ingestion | 사용량 비례 |
| CloudWatch custom metric | 약 0.30 USD/metric-월 |
| CloudWatch standard alarm | 약 0.10 USD/alarm-월 |
| Step Functions Standard | 약 0.025 USD/1,000 transitions |
| EventBridge Scheduler | 현재 호출량은 무료 한도 내 |
| Lambda | 현재 호출량은 무료 한도 내 |

## Environment Scenarios

### aws-paper — 현재 권고

| 항목 | 값 |
| --- | --- |
| 목적 | 모의투자 자동화와 운영 절차 검증 |
| 월 예상 | 약 150~190 USD |
| Network | NAT 미사용 · 최소 Endpoint · Public outbound |
| RDS | `db.t4g.small` single-AZ |
| View | 필요 시 desiredCount 1 |
| Windows EC2 | KRX 작업 시간 중심으로 실행 |
| 장점 | 현재 구조와 실측에 가장 가까움 |
| 주의 | Endpoint 개수·AZ와 Windows EC2 실행 시간을 계속 점검 |

### aws-paper — 상한 모델

| 항목 | 값 |
| --- | --- |
| 월 예상 | 약 180~300 USD |
| 추가 가정 | NAT Gateway · 높은 Logs · 상시 View |
| 용도 | 구조 확장 시 비용 상한 참고 |
| 현재 적용 | 아님 |

### aws-live — 절감형

| 항목 | 값 |
| --- | --- |
| 월 예상 | 약 200~350 USD |
| Network | NAT 기본 미사용 |
| RDS | single-AZ 또는 제한적 Multi-AZ 검토 |
| View | 상시 운영 가능 |
| ALB | 필요성이 확인될 때만 도입 |
| 장점 | 고정비 절감 |
| 주의 | 가용성과 감사 요구를 별도 검토 |

### aws-live — 고가용성형

| 항목 | 값 |
| --- | --- |
| 월 예상 | 약 430~650 USD |
| Network | Multi-AZ NAT 또는 동등한 outbound 구조 |
| RDS | Multi-AZ |
| View | ALB + 상시 Fargate |
| Endpoint | Multi-AZ |
| Observability | Logs·Metrics·Alarm 확대 |
| 장점 | 가용성과 운영 표준 강화 |
| 주의 | 현재 1인 운영 규모에는 과할 수 있음 |

## Cost Reduction Priorities

### 우선순위

| 항목 | 값 |
| --- | --- |
| 1 | Interface Endpoint 개수와 AZ 수 최소화 |
| 2 | NAT Gateway 기본 미사용 유지 |
| 3 | port-view desiredCount 0/1 운영 |
| 4 | Windows EC2 영업시간 중심 start/stop |
| 5 | CloudWatch Logs retention과 ingestion 제한 |
| 6 | RDS instance size와 Multi-AZ 진입 시점 검토 |
| 7 | ALB는 외부 노출 필요 시점에만 도입 |
| 8 | S3 lifecycle과 ECR lifecycle 적용 |

### 절감 시 지켜야 할 원칙

| 항목 | 값 |
| --- | --- |
| 주문 안전 | 비용 절감을 위해 주문 검증이나 승인 gate를 제거하지 않음 |
| 데이터 안정 | RDS backup과 PITR을 무리하게 축소하지 않음 |
| 보안 | RDS Public 전환으로 비용 문제를 해결하지 않음 |
| Endpoint | 삭제 전에 실제 의존 서비스와 outbound 대안을 확인 |
| EC2 | EIP·broker 등록 IP·Windows session 영향을 함께 확인 |
| live | paper 안정 회차와 가용성 요구 확인 후 비용 구조 결정 |

## Budget and Monitoring

| 항목 | 값 |
| --- | --- |
| 월 예산 기준 | 현재 paper 180 USD |
| 경고 기준 | 예산의 80% 도달 |
| 긴급 점검 | 예상 월말 비용이 예산을 20% 초과 |
| 주간 점검 | VPC · RDS · EC2 · CloudWatch 비용 |
| 월간 점검 | 서비스별 실측과 예상 범위 비교 |
| Endpoint 점검 | Endpoint Hours · AZ 수 · 미사용 Endpoint |
| Compute 점검 | EC2 running hours · Fargate task hours |
| Storage 점검 | RDS · S3 · ECR 증가율 |
| 재산정 시점 | live 설계 · ALB 도입 · Multi-AZ 전환 · RDS size 변경 |

## Evidence Management

| 항목 | 값 |
| --- | --- |
| 본 문서 | 실측 비용 · 예상 범위 · 단가 참고 · 절감 원칙 |
| Billing 원문 | 본 문서에 포함하지 않음 |
| 날짜별 비용 분석 | `WORKLOG.md` |
| 리소스 변경 이력 | 관련 spec `operation-notes.md` |
| 운영자 결정 | `operator-decisions.md` |
| 비용 리스크 | `risk-register.md` |
| 상세 계산 | AWS Pricing Calculator |
| Historical Notes | 본 문서에 누적하지 않음 |

## Update Rules

| 항목 | 값 |
| --- | --- |
| 실측 갱신 | 월 청구 확정 후 Dashboard 값 교체 |
| 예상 범위 | 구조나 사용 시간이 바뀔 때만 갱신 |
| 단가 갱신 | Pricing Calculator 확인 후 수정 |
| 날짜별 메모 | 본문에 추가하지 않고 WORKLOG에 기록 |
| 신규 서비스 | 월 1 USD 이상 또는 고정비가 발생할 때 반영 |
| 소액 자동화 | Lambda·Scheduler·Step Functions가 무료 한도면 묶어서 표시 |
| 표 형식 | 독립 표는 `항목 / 값` 2열 |
| 긴 셀 | 여러 행으로 분리 |
| 민감정보 | `[REDACTED]` 계열 placeholder만 사용 |

## Security Notes

| 항목 | 값 |
| --- | --- |
| 실제 AWS 실행 | 없음 |
| AWS 리소스 변경 | 없음 |
| 애플리케이션 코드 수정 | 없음 |
| broker · KIS · DB 실행 | 없음 |
| 민감정보 원문 | 기록 금지 |
| 계정·Billing 식별정보 | 기록 금지 |
| 문서 역할 | AWS Migration 비용 판단 참고 |
