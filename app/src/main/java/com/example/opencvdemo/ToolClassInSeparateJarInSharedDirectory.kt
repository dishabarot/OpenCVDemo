package com.example.opencvdemo


class ToolClassInSeparateJarInSharedDirectory {
    companion object {
        fun loadNativeLibrary() {
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}