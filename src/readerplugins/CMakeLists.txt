add_library (hardcoded SHARED hardcoded.cpp)
set_target_properties (hardcoded PROPERTIES COMPILE_FLAGS "-DBUILDING_RP_DLL")

if (OS_FOUND)
  add_library (optimizationservices SHARED os.cpp)
  set_target_properties (optimizationservices PROPERTIES COMPILE_FLAGS "-DBUILDING_RP_DLL")
  target_link_libraries (optimizationservices ${POCO_LIBRARIES} ${OS_LIBRARIES})
endif ()