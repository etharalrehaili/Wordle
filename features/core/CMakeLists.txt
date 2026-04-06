cmake_minimum_required(VERSION 3.22.1)

project("native-lib")

add_library(
    native-lib
    SHARED
    src/main/cpp/native-lib.cpp
)

find_library(log-lib log)

target_link_libraries(
    native-lib
    ${log-lib}
)

# Keys are injected from local.properties via build.gradle.kts — never hardcoded here
target_compile_definitions(native-lib PRIVATE
    AUTH_TOKEN="${AUTH_TOKEN}"
    BASE_URL="${BASE_URL}"
    FIREBASE_DB_URL="${FIREBASE_DB_URL}"
    GOOGLE_API_KEY="${GOOGLE_API_KEY}"
    BASE_HOST="${BASE_HOST}"
)
