---
name: sync-issue
description: 머지된 PR에 연결된 이슈 본문을 실제 반영 결과에 맞게 수정한다. TODO 체크, 계획과 달라진 점, 미완료 사유, 완료일, 참고 자료를 정리한다.
argument-hint: "[PR 번호, 생략 시 내가 가장 최근에 머지한 PR]"
disable-model-invocation: true
---

# /sync-issue

PR `$ARGUMENTS` 에 연결된 이슈 본문을 머지된 결과에 맞게 고친다.

- 모든 명령은 저장소 루트(`git rev-parse --show-toplevel`)에서 실행한다.
- `gh` 가 PATH에 없으면 `"/c/Program Files/GitHub CLI/gh.exe"` 전체 경로를 쓴다.

## 1. 대상 PR
- 인자가 없으면 `gh pr list --author @me --state merged --limit 1 --json number` 의 PR을 쓴다.
- `gh pr view <PR> --json state,body,mergedAt,baseRefName,closingIssuesReferences`
- `state` 가 `MERGED` 가 아니면 중단한다. 연결된 이슈(`closingIssuesReferences`)가 없으면 중단한다.

## 2. 실제 결과 수집
- PR 본문, `gh pr diff <PR> --name-only`
- `git fetch --prune` 후 머지된 코드는 필요한 파일만 `git show origin/<base>:<path>` 로 확인한다 (예: `backend/build.gradle` 의존성, 실제 생성된 패키지).
- 이슈: `gh issue view <이슈> --json title,state,body`
- PR 본문의 주장만 믿지 말고, TODO마다 코드에서 반영 여부를 확인한다.

## 3. 본문 재작성 규칙
- 본문 상단에 이슈 템플릿 설정 정보(`---` 로 감싼 `name:`, `about:` 등)가 남아 있으면 지운다.
- 섹션 구조는 `.github/ISSUE_TEMPLATE/이슈-템플릿.md` 를 따르고, 빠진 섹션(예: 참고 자료)은 추가한다. 기존 서술은 사실과 다른 부분만 고친다.
- TODO
  - 반영된 항목만 `[x]` 로 체크하고, 실제 구현 내용을 하위 항목으로 적는다.
  - 계획과 다르게 반영된 부분(예: MySQL → H2)은 실제대로 적고 후속 계획을 남긴다.
  - 반영되지 않은 항목은 지우지 않고 `[ ]` 로 두고 사유를 적는다 (예: 후속 이슈에서 진행).
  - 계획에 없었지만 반영된 작업은 `(추가)` 를 붙인 새 항목으로 넣는다.
- 목표 마감일 줄 끝에 `→ **YYYY-MM-DD 완료** (PR #N 머지)` 를 붙인다. 날짜는 `mergedAt` 을 KST로 변환한다.
- 참고 자료에 `PR: #N` 을 넣는다.

## 4. 적용
- 새 본문을 스크래치패드(없으면 임시 디렉터리)에 파일로 쓰고 `gh issue edit <이슈> --body-file <파일>`.
- 제목·라벨·담당자·상태는 바꾸지 않는다. 이슈가 아직 열려 있으면 닫지 말고 그 사실만 보고한다.
- `gh issue view <이슈> --json body` 로 반영을 확인한다.

## 5. 결과 보고
- 이슈 링크, 체크한 항목, 체크하지 않은 항목과 사유, 계획과 달라진 점을 짧게 요약한다.
- 체크하지 않은 항목이 있으면, 후속 이슈로 만들지 마지막에 한 번만 묻는다.
