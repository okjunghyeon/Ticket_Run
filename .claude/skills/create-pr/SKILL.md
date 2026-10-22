---
name: create-pr
description: 현재 작업 브랜치로 GitHub PR을 생성한다. PR 템플릿 작성, 이슈와 같은 제목·라벨, assignee, 프로젝트 연결(Status=In review), close #N 까지 처리한다.
argument-hint: "[base 브랜치, 기본 dev]"
disable-model-invocation: true
---

# /create-pr

현재 브랜치를 `$ARGUMENTS` 브랜치(비어 있으면 `dev`)로 머지하는 PR을 만든다.

- 모든 명령은 저장소 루트(`git rev-parse --show-toplevel`)에서 실행한다.
- `gh` 가 PATH에 없으면 `"/c/Program Files/GitHub CLI/gh.exe"` 전체 경로를 쓴다.
- 기본 프로젝트: `@okjunghyeon's Ticket_Run project` (owner `okjunghyeon`)

## 1. 사전 확인 (하나라도 걸리면 중단하고 이유를 보고)
- 현재 브랜치가 `main` / `dev` 이면 중단한다.
- `git status --porcelain` 에 커밋하지 않은 변경이 있으면 중단한다.
- `git fetch --prune` 후 원격보다 앞선 커밋이 있으면 `git push -u origin HEAD`.
- `gh pr list --head <브랜치> --state open` 에 이미 PR이 있으면 새로 만들지 않고 링크만 안내한다.

## 2. 이슈 번호
- 브랜치명 `<type>/#<N>-...` 에서 `N` 을 추출한다.
- 추출되지 않으면 사용자에게 이슈 번호를 묻는다. 이 skill에서 질문하는 곳은 여기뿐이다.

## 3. 정보 수집
- 이슈: `gh issue view N --json title,labels,assignees,projectItems`
- 변경: `git log --oneline origin/<base>..HEAD`, `git diff --stat origin/<base>...HEAD`, 필요한 파일은 `git diff origin/<base>...HEAD -- <path>`

## 4. 검증
- `backend/` 에 변경이 있으면 `backend/` 에서 `./gradlew build` 를 실행한다.
- 실패하면 PR을 만들지 않고 실패 내용을 보고한다.
- 결과는 본문 '테스트 방법' 에 사실대로 적는다. 실행하지 않은 항목은 체크하지 않는다.

## 5. 본문 작성
- `.github/ISSUE_TEMPLATE/pull_request_template.md` 를 읽고 섹션 구조를 그대로 채운다.
- 커밋·diff에서 확인한 내용만 쓴다. 해당 없는 체크 항목은 `(해당 없음)` 으로 표기한다.
- 관련 이슈는 `close #N`.
- 본문 끝에 다음 줄을 붙인다: `🤖 Generated with [Claude Code](https://claude.com/claude-code)`
- 본문은 스크래치패드(없으면 임시 디렉터리)에 파일로 쓰고 `--body-file` 로 넘긴다.

## 6. 생성
```
gh pr create --base <base> --head <브랜치> --title "<이슈 제목>" \
  --body-file <파일> --assignee @me --label <이슈 라벨1> --label <이슈 라벨2>
```

## 7. 후처리
- 프로젝트: 이슈의 `projectItems` 에 프로젝트가 있으면 그것을, 없으면 기본 프로젝트를 쓴다.
  - `gh pr edit <PR> --add-project "<프로젝트명>"`
  - `gh project item-edit <프로젝트 번호> --owner <owner> --url <PR url> --field Status --value "In review"`
  - 프로젝트 번호는 `gh project list --owner <owner>` 로 찾는다.
- 리뷰어: `gh api repos/{owner}/{repo}/collaborators --jq '.[].login'` 에서 본인을 뺀 인원이 있으면 `gh pr edit <PR> --add-reviewer` 로 지정한다. 없으면 건너뛰었다고 보고한다 (작성자는 자기 PR의 리뷰어가 될 수 없다).

## 8. 결과 보고
`gh pr view <PR> --json title,baseRefName,headRefName,assignees,labels,projectItems,reviewRequests,closingIssuesReferences` 로 다시 조회해 PR 링크와 함께 표로 보고한다. 설정하지 못한 항목은 이유를 적는다.
