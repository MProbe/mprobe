#include public headers
include_directories (${CMAKE_SOURCE_DIR}/include/mprobe/)
include_directories (${CMAKE_SOURCE_DIR}/include/common/)

add_subdirectory (mprobe)
add_subdirectory (readerplugins)
add_subdirectory (standaloneexample)