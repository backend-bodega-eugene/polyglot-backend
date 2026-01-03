add_executable(eugene_image_demo WIN32 main.cpp "stb_image.h")
target_link_libraries(
	eugene_image_demo
	eugene_image_lib
	imgui
	d3d11
	dxgi
)
target_include_directories(eugene_image_demo PRIVATE
    ${CMAKE_SOURCE_DIR}/third_party/imgui
    ${CMAKE_SOURCE_DIR}/third_party/imgui/backends
)