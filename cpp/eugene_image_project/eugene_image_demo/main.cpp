#undef IMGUI_DISABLE_WIN32_FUNCTIONS
#define STB_IMAGE_IMPLEMENTATION
#include <windows.h>
#include <d3d11.h>
#include <tchar.h>
#include "stb_image.h"
#include "imgui.h"
#include "backends/imgui_impl_win32.h"
#include "backends/imgui_impl_dx11.h"




#pragma comment(lib, "d3d11.lib")
#pragma comment(lib, "dxgi.lib")

static ID3D11Device* g_pd3dDevice = nullptr;
static ID3D11DeviceContext* g_pd3dDeviceContext = nullptr;
static IDXGISwapChain* g_pSwapChain = nullptr;
static ID3D11RenderTargetView* g_mainRenderTargetView = nullptr;
ID3D11ShaderResourceView* g_OriginalImageSRV = nullptr;
ID3D11ShaderResourceView* g_ProcessedImageSRV = nullptr;

int g_ImageWidth = 0, g_ImageHeight = 0;

void CreateRenderTarget();
void CleanupRenderTarget();
bool OpenImageDialog(HWND owner, char* outPath, int maxLen);
bool LoadTextureFromFile(const char* filename,
    ID3D11Device* device,
    ID3D11ShaderResourceView** out_srv,
    int* out_width,
    int* out_height);
void ClearImages();
extern IMGUI_IMPL_API LRESULT ImGui_ImplWin32_WndProcHandler(
    HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam
);
void Resize(UINT width, UINT height)
{
    if (g_pd3dDevice != nullptr && g_pSwapChain != nullptr)
    {
        CleanupRenderTarget();
        g_pSwapChain->ResizeBuffers(0, width, height, DXGI_FORMAT_UNKNOWN, 0);
        CreateRenderTarget();
    }
}

void CreateRenderTarget()
{
    ID3D11Texture2D* pBackBuffer = nullptr;
    g_pSwapChain->GetBuffer(0, IID_PPV_ARGS(&pBackBuffer));
    g_pd3dDevice->CreateRenderTargetView(pBackBuffer, nullptr, &g_mainRenderTargetView);
    pBackBuffer->Release();
}

void CleanupRenderTarget()
{
    if (g_mainRenderTargetView) { g_mainRenderTargetView->Release(); g_mainRenderTargetView = nullptr; }
}

bool CreateDeviceD3D(HWND hWnd)
{
    DXGI_SWAP_CHAIN_DESC sd{};
    sd.BufferCount = 2;
    sd.BufferDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
    sd.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
    sd.OutputWindow = hWnd;
    sd.SampleDesc.Count = 1;
    sd.Windowed = TRUE;

    return D3D11CreateDeviceAndSwapChain(nullptr, D3D_DRIVER_TYPE_HARDWARE, nullptr, 0,
        nullptr, 0, D3D11_SDK_VERSION, &sd, &g_pSwapChain,
        &g_pd3dDevice, nullptr, &g_pd3dDeviceContext) == S_OK;
}

void CleanupDeviceD3D()
{
    CleanupRenderTarget();
    if (g_pSwapChain) { g_pSwapChain->Release(); g_pSwapChain = nullptr; }
    if (g_pd3dDeviceContext) { g_pd3dDeviceContext->Release(); g_pd3dDeviceContext = nullptr; }
    if (g_pd3dDevice) { g_pd3dDevice->Release(); g_pd3dDevice = nullptr; }
}

LRESULT WINAPI WndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
    if (ImGui_ImplWin32_WndProcHandler(hWnd, msg, wParam, lParam))
        return true;
    switch (msg)
    {
    case WM_SIZE:
        if (wParam != SIZE_MINIMIZED)
            Resize((UINT)LOWORD(lParam), (UINT)HIWORD(lParam));
        return 0;

    case WM_DESTROY:
        PostQuitMessage(0);
        return 0;
    }
    return DefWindowProc(hWnd, msg, wParam, lParam);
}


int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE, LPSTR, int)
{
    WNDCLASSEX wc{ sizeof(WNDCLASSEX), CS_CLASSDC, WndProc, 0L, 0L, hInstance, nullptr, nullptr, nullptr, nullptr, _T("ImageLab"), nullptr };
    RegisterClassEx(&wc);
    HWND hwnd = CreateWindow(wc.lpszClassName, _T("Eugene Image Lab"), WS_OVERLAPPEDWINDOW, 100, 100, 1280, 800, nullptr, nullptr, wc.hInstance, nullptr);

    if (!CreateDeviceD3D(hwnd)) return 1;
    CreateRenderTarget();
    ShowWindow(hwnd, SW_SHOWDEFAULT);
    UpdateWindow(hwnd);

    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io = ImGui::GetIO(); (void)io;

    ImGui_ImplWin32_Init(hwnd);
    ImGui_ImplDX11_Init(g_pd3dDevice, g_pd3dDeviceContext);

    while (true)
    {
        MSG msg;
        while (PeekMessage(&msg, nullptr, 0U, 0U, PM_REMOVE))
        {
            TranslateMessage(&msg);
            DispatchMessage(&msg);
            if (msg.message == WM_QUIT) goto cleanup;
        }

        ImGui_ImplDX11_NewFrame();
        ImGui_ImplWin32_NewFrame();
        ImGui::NewFrame();

        ImGuiIO& io = ImGui::GetIO();
        ImGui::SetNextWindowPos(ImVec2(0, 0));
        ImGui::SetNextWindowSize(io.DisplaySize);

        ImGui::Begin("Eugene Image Lab", nullptr,
            ImGuiWindowFlags_NoTitleBar |
            ImGuiWindowFlags_NoResize |
            ImGuiWindowFlags_NoMove |
            ImGuiWindowFlags_NoCollapse |
            ImGuiWindowFlags_NoBringToFrontOnFocus);

        if (ImGui::Button("Upload Image")) {
            
            /* TODO */
                static char imagePath[260]{};
                if (OpenImageDialog(hwnd, imagePath, sizeof(imagePath)))
                {
                    if (g_OriginalImageSRV) { g_OriginalImageSRV->Release(); g_OriginalImageSRV = nullptr; }

                    LoadTextureFromFile(imagePath, g_pd3dDevice,
                        &g_OriginalImageSRV, &g_ImageWidth, &g_ImageHeight);
                }
        
        }
        ImGui::SameLine();
        if (ImGui::Button("Grayscale")) { /* TODO */ }
        ImGui::SameLine();
        if (ImGui::Button("Flip")) { /* TODO */ }
        ImGui::SameLine();
        if (ImGui::Button("Binarize")) { /* TODO */ }
        ImGui::SameLine();
        if (ImGui::Button("Clear"))
        {
            ClearImages();
        }
        ImGui::Separator();

        float panelHeight = ImGui::GetContentRegionAvail().y;
        float panelWidth = ImGui::GetContentRegionAvail().x;

        float imagePanelWidth = (panelWidth - 20.0f) * 0.5f;

        ImGui::BeginChild("OriginalPanel", ImVec2(imagePanelWidth, panelHeight), true);
        ImGui::Text("Original");
        ImGui::Dummy(ImVec2(0, 10));
        //ImGui::Button("Original Image Here", ImVec2(imagePanelWidth - 20, panelHeight - 60));
        if (g_OriginalImageSRV)
        {
            ImVec2 size = ImVec2((float)g_ImageWidth, (float)g_ImageHeight);
            ImGui::Image((void*)g_OriginalImageSRV, size);
        }
        else
        {
            ImGui::Button("Original Image Here", ImVec2(imagePanelWidth - 20, panelHeight - 60));
        }

        ImGui::EndChild();

        ImGui::SameLine();

        ImGui::BeginChild("ProcessedPanel", ImVec2(imagePanelWidth, panelHeight), true);
        ImGui::Text("Processed");
        ImGui::Dummy(ImVec2(0, 10));
        //ImGui::Button("Processed Image Here", ImVec2(imagePanelWidth - 20, panelHeight - 60));
        if (g_ProcessedImageSRV)
        {
            ImGui::Image((void*)g_ProcessedImageSRV, ImVec2((float)g_ImageWidth, (float)g_ImageHeight));
        }
        else
        {
            ImGui::Button("Processed Image Here", ImVec2(imagePanelWidth - 20, panelHeight - 60));
        }

        ImGui::EndChild();

        ImGui::End();


        ImGui::Render();
        const float clear_color[4] = { 0.1f, 0.1f, 0.1f, 1.0f };
        g_pd3dDeviceContext->OMSetRenderTargets(1, &g_mainRenderTargetView, nullptr);
        g_pd3dDeviceContext->ClearRenderTargetView(g_mainRenderTargetView, clear_color);
        ImGui_ImplDX11_RenderDrawData(ImGui::GetDrawData());
        g_pSwapChain->Present(1, 0);
    }

cleanup:
    ImGui_ImplDX11_Shutdown();
    ImGui_ImplWin32_Shutdown();
    ImGui::DestroyContext();
    CleanupDeviceD3D();
    DestroyWindow(hwnd);
    UnregisterClass(wc.lpszClassName, wc.hInstance);
    return 0;
}

bool OpenImageDialog(HWND owner, char* outPath, int maxLen)
{
    OPENFILENAMEA ofn{};
    ZeroMemory(&ofn, sizeof(ofn));
    ZeroMemory(outPath, maxLen);

    ofn.lStructSize = sizeof(ofn);
    ofn.hwndOwner = owner;
    ofn.lpstrFilter = "Image Files\0*.png;*.jpg;*.bmp\0";
    ofn.lpstrFile = outPath;
    ofn.nMaxFile = maxLen;
    ofn.Flags = OFN_PATHMUSTEXIST | OFN_FILEMUSTEXIST;

    return GetOpenFileNameA(&ofn);
}
bool LoadTextureFromFile(const char* filename,
    ID3D11Device* device,
    ID3D11ShaderResourceView** out_srv,
    int* out_width,
    int* out_height)
{
    int w, h, n;
    unsigned char* data = stbi_load(filename, &w, &h, &n, 4);
    if (!data) return false;

    D3D11_TEXTURE2D_DESC desc{};
    desc.Width = w;
    desc.Height = h;
    desc.MipLevels = 1;
    desc.ArraySize = 1;
    desc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
    desc.SampleDesc.Count = 1;
    desc.Usage = D3D11_USAGE_DEFAULT;
    desc.BindFlags = D3D11_BIND_SHADER_RESOURCE;

    D3D11_SUBRESOURCE_DATA sub{};
    sub.pSysMem = data;
    sub.SysMemPitch = w * 4;

    ID3D11Texture2D* tex = nullptr;
    device->CreateTexture2D(&desc, &sub, &tex);

    D3D11_SHADER_RESOURCE_VIEW_DESC srvDesc{};
    srvDesc.Format = desc.Format;
    srvDesc.ViewDimension = D3D11_SRV_DIMENSION_TEXTURE2D;
    srvDesc.Texture2D.MipLevels = 1;

    device->CreateShaderResourceView(tex, &srvDesc, out_srv);
    tex->Release();
    stbi_image_free(data);

    *out_width = w;
    *out_height = h;
    return true;
}
void ClearImages()
{
    if (g_OriginalImageSRV) {
        g_OriginalImageSRV->Release();
        g_OriginalImageSRV = nullptr;
    }

    if (g_ProcessedImageSRV) {
        g_ProcessedImageSRV->Release();
        g_ProcessedImageSRV = nullptr;
    }

    g_ImageWidth = 0;
    g_ImageHeight = 0;
}

