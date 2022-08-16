package net.liplum.mdt.animation

import arc.graphics.Color
import net.liplum.mdt.render.Draw
import net.liplum.mdt.render.ResetDraw
import net.liplum.mdt.render.SetColor
import plumy.core.assets.TR

interface IFramed {
    val curFrame: TR
}

fun IFramed.draw(x: Float, y: Float, rotation: Float = 0f) {
    curFrame.Draw(x, y, rotation)
}

fun IFramed.draw(color: Color, x: Float, y: Float, rotation: Float = 0f) {
    SetColor(color)
    curFrame.Draw(x, y, rotation)
    ResetDraw()
}

inline fun IFramed.draw(draw: (TR) -> Unit) {
    draw(curFrame)
}