set (STANDALONEEXAMPLE_SOURCES
  standaloneexample.cpp
)

add_executable (standaloneexample ${STANDALONEEXAMPLE_SOURCES})
target_link_libraries (standaloneexample mprobe)

# vim: ts=2:sw=2
