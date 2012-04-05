set (MPROBE_SOURCES
  analysis.cpp
  histogram.cpp
  mprobe.cpp
  probleminstance.cpp
  pluginmanager.cpp
  probleminstancefactory.cpp
  readerplugin.cpp
)

add_library (mprobe SHARED ${MPROBE_SOURCES})
target_link_libraries (mprobe ${POCO_LIBRARIES} ${Boost_LIBRARIES})

include_directories (${CMAKE_CURRENT_SOURCE_DIR})


# vim: ts=2:sw=2