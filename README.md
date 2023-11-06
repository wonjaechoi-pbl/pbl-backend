# kb-springboot
KB 시간제보험 스프링부트 backend

***
## 개발환경

- SDK : 11.0.18
- Language level : 11
- IDE : Intellij 2022.1
- Framework : Springboot(3.x)
- Database : MariaDB
- ORM : JPA(Java Persisitence API) + querydsl

***

## Git Flow
- main : Application을 배포하는 branch 입니다.
- develop : 개발 branch로 feature에서 개발한 기능을 merge 를 할때 기준이 됩니다.
- feature : 단위 기능을 개발하는 branch로 기능 개발이 완료되면 develop branch에 merge를 합니다.
- release : main branch로 push 전에, QA를 위한 branch 입니다.
- hotfix : 배포한 main branch에 버그가 있을 시 긴급 fix하는 branch 입니다.

### branch 생성
```
git checkout develop
git branch [브랜치명]
git push --set-upstream origin [생성한 브랜치명]
```

### commit message
- feat : 새로운 기능 추가, 기존의 기능을 요구 사항에 맞추어 수정
- fix : 기능에 대한 버그 수정
- build : 빌드 관련 수정
- chore : 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore
- docs : 문서(주석) 수정
- style : 코드 스타일, 포맷팅에 대한 수정
- refactor : 기능의 변화가 아닌 코드 리팩터링 ex) 변수 이름 변경
- test : 테스트 코드 추가/수정
- hotfix : 급하게 치명적인 버그를 수정


[예시]
```
feat: 회원 정보 조회 API

회원의 ID, 생년월일, 이름, 전화번호를 조회 하기 위한 List 조회 API 개발 완료.
```


---
v0.0.1 | 2023-08-25 