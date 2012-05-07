##################################
# Basic CMake Configuration
##################################
cmake_minimum_required (VERSION 2.8)
set (CMAKE_COLOR_MAKEFILE TRUE)
set (CMAKE_ALLOW_LOOSE_LOOP_CONSTRUCTS TRUE)
cmake_policy (SET CMP0003 NEW)
cmake_policy (SET CMP0005 NEW)

# Use relative paths
# This is mostly to reduce path size for command-line limits on windows
if (WIN32)
  # This supposedly breaks Xcode projects so don't enable on Apple builds
  set (CMAKE_USE_RELATIVE_PATHS true)
  set (CMAKE_SUPPRESS_REGENERATION true)
endif ()

project (mprobe) # CXX and C are default languages.

# Include Submodules
set (CMAKE_MODULE_PATH
  ${CMAKE_SOURCE_DIR}/cmake/modules
)
include (FindPkgConfig)

#####################################################################
# Set up the basic build environment
#####################################################################

if (CMAKE_BUILD_TYPE STREQUAL "")
  # CMake defaults to leaving CMAKE_BUILD_TYPE empty. This screws up
  # differentiation between debug and release builds.
  set (CMAKE_BUILD_TYPE "RelWithDebInfo" CACHE STRING "Choose the type of build, options are: None (CMAKE_CXX_FLAGS or CMAKE_C_FLAGS used) Debug Release RelWithDebInfo MinSizeRel." FORCE)
endif ()

if (MSVC)
  if (CMAKE_BUILD_TOOL STREQUAL "nmake")
    # set variable to state that we are using nmake makefiles
  set (NMAKE TRUE)
  endif ()
endif ()

if (MINGW)
  add_definitions (-D_WIN32_WINNT=0x0500) # enable Win 2k / Win XP APIs on mingw
endif ()

if (UNIX AND NOT WIN32)
  string (REGEX MATCH [fF][rR][eE]{2}[bB][sS][dD] IS_FREEBSD ${CMAKE_SYSTEM_NAME})
  if (NOT IS_FREEBSD)
    add_definitions (-D_XOPEN_SOURCE=600)
  endif ()
endif ()

# Set compiler specific build flags
if (CMAKE_COMPILER_IS_GNUCXX)
  # Set our own default flags at first run.
  if (NOT CONFIGURED)

    if (ENABLE_STRICT_COMPILATION)
      set (STRICT_FLAGS "-ansi -Wextra -pedantic -Werror -Wno-strict-aliasing")
    else (ENABLE_STRICT_COMPILATION)
      set (STRICT_FLAGS "")
    endif ()

    # GCC >= 4.0
    exec_program (
      ${CMAKE_C_COMPILER}
      ARGS ${CMAKE_C_COMPILER_ARG1} -dumpversion
      OUTPUT_VARIABLE GCC_VERSION
    )
    if (GCC_VERSION MATCHES "4\\.[0-9]\\.[0-9]")
      set (SYM_VISIBILITY_FLAGS "-fvisibility=hidden" CACHE INTERNAL "")
    else ()
      set (SYM_VISIBILITY_FLAGS "" CACHE INTERNAL "")
    endif ()

    # CMAKE_C_FLAGS gets appended to the other C flags
    set(CMAKE_C_FLAGS "-Wall $ENV{CFLAGS}"
        CACHE STRING "Flags used by the C compiler during normal builds." FORCE)

    set(CMAKE_C_FLAGS_DEBUG "-g -DDDEBUG"
        CACHE STRING "Flags used by the C compiler during debug builds." FORCE)
    set(CMAKE_C_FLAGS_RELEASE "-O2 -DNDEBUG"
        CACHE STRING "Flags used by the C compiler during release builds." FORCE)
    set(CMAKE_C_FLAGS_RELWITHDEBINFO "-O2 -g -DNDEBUG"
        CACHE STRING "Flags used by the C compiler during release builds." FORCE)
    set(CMAKE_C_FLAGS_MINSIZEREL "-Os -DNDEBUG"
        CACHE STRING "Flags used by the C compiler during release builds." FORCE)

    # CMAKE_CXX_FLAGS gets appended to the other CXX flags
    set(CMAKE_CXX_FLAGS "-Wall ${STRICT_FLAGS} $ENV{CXXFLAGS}"
        CACHE STRING "Flags used by the CXX compiler during normal builds." FORCE)

    set(CMAKE_CXX_FLAGS_DEBUG "-g -DDEBUG"
        CACHE STRING "Flags used by the CXX compiler during debug builds." FORCE)
    set(CMAKE_CXX_FLAGS_RELEASE "-O2 -DNDEBUG"
        CACHE STRING "Flags used by the CXX compiler during release builds." FORCE)
    set(CMAKE_CXX_FLAGS_RELWITHDEBINFO "-O2 -g -DNDEBUG"
        CACHE STRING "Flags used by the CXX compiler during release builds." FORCE)
    set(CMAKE_CXX_FLAGS_MINSIZEREL "-Os -DNDEBUG"
        CACHE STRING "Flags used by the CXX compiler during release builds." FORCE)

  endif (NOT CONFIGURED)
endif ()

#######################################################################
# Dependencies
#######################################################################

find_package (POCO QUIET REQUIRED)
pkg_check_modules (OS os)
find_package(Boost 1.48.0 COMPONENTS filesystem REQUIRED)

if (OS_FOUND)
  include_directories(${OS_INCLUDE_DIRS})
  link_directories (${OS_LIBRARY_DIRS})
endif ()

if (WIN32)
	add_definitions (-DPOCO_WIN32_UTF8)
endif ()

# Add library and include paths from the dependencies

include_directories (${POCO_INCLUDE_DIRS} ${Boost_INCLUDE_DIRS})

set (CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set (CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib) # Note that static libraries are treated as archive targets
set (CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

######################################################################
# Provide user options to customise the build process
######################################################################

# hide advanced options
#mark_as_advanced ()
###################################################################
# configure global build settings based on selected build options
###################################################################

##################################################################
# Now setup targets
##################################################################
add_subdirectory (3rdparty)
add_subdirectory (src)


## Other stuff
set (CONFIGURED YES CACHE INTERNAL "")

# vim: ts=2:sw=2