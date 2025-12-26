add_library(eugene_image_lib
    src/flip_horizontal.cpp
    src/flip_vertical.cpp
    src/grayscale.cpp
    src/image_loader.cpp
    src/save_image.cpp
    src/threshold.cpp
)

target_include_directories(eugene_image_lib PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)
