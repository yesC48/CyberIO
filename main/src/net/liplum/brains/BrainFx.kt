package net.liplum.brains

import arc.graphics.Color
import arc.graphics.Texture.TextureFilter
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import arc.util.Time
import mindustry.entities.Effect
import mindustry.gen.EffectState
import mindustry.gen.Posc
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import net.liplum.Cio
import net.liplum.R
import net.liplum.ResourceLoader
import net.liplum.annotations.Only
import net.liplum.annotations.SubscribeEvent
import net.liplum.events.CioLoadContentEvent
import net.liplum.lib.TR
import net.liplum.lib.entity.FixedList
import net.liplum.lib.entity.Radiation
import net.liplum.lib.entity.RadiationArray
import net.liplum.lib.utils.progress
import net.liplum.mdt.render.DrawSize
import net.liplum.mdt.WhenNotPaused
import net.liplum.mdt.utils.MdtUnit
import net.liplum.mdt.utils.NewEffect
import net.liplum.mdt.utils.fadeInOutPct
import net.liplum.mdt.utils.sheet

object BrainFx {
    val eyeCharge = NewEffect(38f) {
        Draw.color(R.C.RedDark)
        Angles.randLenVectors(
            id.toLong(), 2, 1f + 20f * fout(), rotation, 120f
        ) { x: Float, y: Float ->
            Lines.lineAngle(
                x + x,
                y + y,
                Mathf.angle(x, y),
                fslope() * 3f + 1f
            )
        }
    }
    val eyeChargeBegin = NewEffect(60f) {
        Draw.color(R.C.RedDark)
        Fill.circle(x, y, fin() * 3f)
    }
    val eyeShoot = NewEffect(21f) {
        Draw.color(R.C.RedAlert)
        for (i in Mathf.signs) {
            Drawf.tri(x, y, 4f * fout(), 29f, rotation + 90f * i)
        }
    }
    val mindControlled = MindControlFx(60f)
    var bloodBulletFrames: Array<TR> = emptyArray()
    val bloodBulletHit = Effect(30f) {
        val scale = it.data as? Float ?: -1f
        Draw.mixcol(it.color, it.color.a)
        bloodBulletFrames.progress(it.fin())
            .DrawSize(it.x, it.y, if (scale < 0f) 1f else scale)
    }.apply {
        layer = Layer.bullet - 0.1f
    }
    @JvmStatic
    @SubscribeEvent(CioLoadContentEvent::class, Only.client)
    fun load() {
        ResourceLoader += {
            bloodBulletFrames = "blood-bullet-hit".Cio.sheet(16)
            if (bloodBulletFrames.isNotEmpty())
                bloodBulletFrames[0].texture.setFilter(TextureFilter.nearest)
        }
    }
}

class MindControlFx : Effect {
    constructor() : super()
    constructor(lifetime: Float) {
        this.lifetime = lifetime
    }

    var maxRange = 20f
    var width = 2f
    var widthSpeed = 0.4f
    override fun add(x: Float, y: Float, rotation: Float, color: Color, data: Any?) {
        val entity = EffectState.create()
        entity.effect = this
        entity.rotation = baseRotation + rotation
        entity.data = data
        entity.lifetime = lifetime
        entity.set(x, y)
        entity.color.set(color)
        entity.add()
        if (followParent && data is Tracker) {
            entity.parent = data.target
            entity.rotWithParent = rotWithParent
        }
    }

    fun atUnit(unit: MdtUnit, radiationNumber: Int) {
        this.at(
            unit.x, unit.y, 0f,
            Tracker(unit,
                RadiationArray(radiationNumber) { i, r ->
                    r.range = maxRange * i / 3
                })
        )
    }
    @Suppress("UNCHECKED_CAST")
    override fun render(e: EffectContainer) = e.run {
        val tracker = data as? Tracker
        if (tracker != null) {
            val waves = tracker.radiations
            WhenNotPaused {
                waves.forEach {
                    it.range += widthSpeed * Time.delta
                    it.range %= maxRange
                }
            }
            waves.forEach {
                Lines.stroke(2.5f, R.C.BrainWave)
                Draw.alpha(fadeInOutPct(0.2f))
                Lines.circle(x, y, it.range)
            }
            Draw.reset()
        }
    }

    class Tracker(
        val target: Posc,
        val radiations: FixedList<Radiation>
    )
}