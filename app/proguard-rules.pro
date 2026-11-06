# Gson은 필드 이름으로 JSON을 읽고 쓴다. 서버 응답·시간표 캐시 DTO의 필드 이름이 바뀌면 값이 비어 버린다.
-keep class com.tukorea.bus.data.model.** { <fields>; <init>(...); }

# 노선 경로(assets/shuttle_routes.json)를 TypeToken으로 읽는다. 제네릭 정보가 지워지면 읽지 못한다.
-keepattributes Signature
-keep class * extends com.google.gson.reflect.TypeToken
