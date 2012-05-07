
set (GAMS_API_BASE_URL "http://www.gams.com/dd/apis")
set (GAMS_C_API_FILES
  dctmcc.h
  dctmcc.c
  gclgms.h
  gclgms.c
  gmomcc.h
  gmomcc.c
  gamsxcc.h
  gamsxcc.c
  gdxcc.h
  gdxcc.c
  gevmcc.h
  gevmcc.c
  optcc.h
  optcc.c
)
set (GAMS_CPP_API_FILES
  dctmco.hpp
  dctmco.cpp
  gamsxco.hpp
  gamsxco.cpp
  gdxco.hpp
  gdxco.cpp
  gevmco.hpp
  gevmco.cpp
  gmomco.hpp
  gmomco.cpp
  optco.hpp
  optco.cpp
)

if (NOT EXISTS ${CMAKE_CURRENT_BINARY_DIR}/C/)
  file (MAKE_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/C)
endif ()
if (NOT EXISTS ${CMAKE_CURRENT_BINARY_DIR}/C++/)
  file (MAKE_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/C++)
endif ()

foreach (file ${GAMS_C_API_FILES})
  if (NOT EXISTS ${CMAKE_CURRENT_BINARY_DIR}/C/${file})
    message (STATUS "Downloading GAMS API file: ${file}")
    file (
      DOWNLOAD "${GAMS_API_BASE_URL}/C/api/${file}"
      "${CMAKE_CURRENT_BINARY_DIR}/C/${file}"
      STATUS status)
    list(GET status 0 retval)
    list(GET status 1 statusstr)
    if (retval)
      message (SEND_ERROR "Error downloading GAMS API file ${file}:\n	Reason:${statusstr}")
      file (REMOVE "${CMAKE_CURRENT_BINARY_DIR}/C++/${file}")
    endif()
  endif ()
endforeach ()
foreach (file ${GAMS_CPP_API_FILES})
  if (NOT EXISTS ${CMAKE_CURRENT_BINARY_DIR}/C++/${file})
    message (STATUS "Downloading GAMS API file: ${file}")
    file (
      DOWNLOAD "${GAMS_API_BASE_URL}/C++/api/${file}"
      "${CMAKE_CURRENT_BINARY_DIR}/C++/${file}"
      STATUS status)
    list(GET status 0 retval)
    list(GET status 1 statusstr)
    if (retval)
      message (SEND_ERROR "Error downloading GAMS API file ${file}:\n	Reason:${statusstr}")
      file (REMOVE "${CMAKE_CURRENT_BINARY_DIR}/C++/${file}")
    endif()
  endif ()
endforeach ()

set (GAMS_API_SOURCES
  ${CMAKE_CURRENT_BINARY_DIR}/C/dctmcc.c
  ${CMAKE_CURRENT_BINARY_DIR}/C/gamsxcc.c
  ${CMAKE_CURRENT_BINARY_DIR}/C/gdxcc.c
  ${CMAKE_CURRENT_BINARY_DIR}/C/gevmcc.c
  ${CMAKE_CURRENT_BINARY_DIR}/C/gmomcc.c
  ${CMAKE_CURRENT_BINARY_DIR}/C/optcc.c
  ${CMAKE_CURRENT_BINARY_DIR}/C++/dctmco.cpp
  ${CMAKE_CURRENT_BINARY_DIR}/C++/gamsxco.cpp
  ${CMAKE_CURRENT_BINARY_DIR}/C++/gdxco.cpp
  ${CMAKE_CURRENT_BINARY_DIR}/C++/gevmco.cpp
  ${CMAKE_CURRENT_BINARY_DIR}/C++/gmomco.cpp
  ${CMAKE_CURRENT_BINARY_DIR}/C++/optco.cpp
  CACHE INTERNAL ""
)

set_source_files_properties (${GAMS_API_SOURCES} GENERATED)

set (GAMS_API_INCLUDE_DIRS ${CMAKE_CURRENT_BINARY_DIR}/C ${CMAKE_CURRENT_BINARY_DIR}/C++ CACHE INTERNAL "")
