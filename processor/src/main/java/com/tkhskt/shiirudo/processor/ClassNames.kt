package com.tkhskt.shiirudo.processor

import com.squareup.kotlinpoet.ClassName

internal object ClassNames {
    private const val PKG_ANNOTATION = "com.tkhskt.shiirudo.annotation"

    val SHIIRUDO = ClassName(PKG_ANNOTATION, "Shiirudo")
}
