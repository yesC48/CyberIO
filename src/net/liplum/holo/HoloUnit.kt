package net.liplum.holo

import arc.Events
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.content.Fx
import mindustry.entities.abilities.ForceFieldAbility
import mindustry.game.EventType.UnitDestroyEvent
import mindustry.gen.UnitEntity
import mindustry.logic.LAccess
import net.liplum.R
import net.liplum.registries.EntityRegistry
import net.liplum.utils.hasShields

open class HoloUnit : UnitEntity() {
    @JvmField var time = 0f
    val HoloType: HoloUnitType
        get() = type as HoloUnitType
    open val lifespan: Float
        get() = HoloType.lifespan
    open val overageDmgFactor: Float
        get() = HoloType.overageDmgFactor
    open val lose: Float
        get() = HoloType.lose
    open val restLifePercent: Float
        get() = (1f - (time / lifespan)).coerceIn(0f, 1f)
    open val restLife: Float
        get() = (lifespan - time).coerceIn(0f, lifespan)

    override fun update() {
        time += Time.delta
        super.update()
        val lose = lose
        var damage = lose
        val overage = time - lifespan
        if (overage > 0) {
            damage += overage * lose * overageDmgFactor
        }
        damageByHoloDimming(damage)
    }

    override fun destroy() {
        if (this.isAdded) {
            type.deathSound.at(this)
            Events.fire(UnitDestroyEvent(this))
            for (mount in mounts) {
                if (mount.weapon.shootOnDeath && (!mount.weapon.bullet.killShooter || !mount.shoot)) {
                    mount.reload = 0.0f
                    mount.shoot = true
                    mount.weapon.update(this, mount)
                }
            }
            for (ability in abilities) {
                ability.death(this)
            }
            this.remove()
        }
    }

    open fun damageByHoloDimming(amount: Float) {
        val hasShields = this.hasShields
        if (hasShields) {
            shieldAlpha = 1.0f
        }
        health -= amount
        if (health <= 0.0f && !dead) {
            if (hasShields) {
                Time.run(30f) {
                    (abilities.find {
                        it is ForceFieldAbility
                    } as? ForceFieldAbility)?.let {
                        Fx.shieldBreak.at(x, y, it.radius, R.C.Holo)
                    }
                }
            }
            kill()
        }
    }

    override fun classId(): Int {
        return EntityRegistry.getID(javaClass)
    }

    override fun read(read: Reads) {
        super.read(read)
        time = read.f()
    }

    override fun write(write: Writes) {
        super.write(write)
        write.f(time)
    }

    override fun readSync(read: Reads) {
        super.readSync(read)
        time = read.f()
    }

    override fun writeSync(write: Writes) {
        super.writeSync(write)
        write.f(time)
    }

    override fun sense(sensor: LAccess): Double {
        return when (sensor) {
            LAccess.progress -> (1f - restLifePercent).toDouble()
            else -> super.sense(sensor)
        }
    }
}