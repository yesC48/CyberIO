package net.liplum.api.cyber

import mindustry.Vars
import mindustry.world.Block
import net.liplum.api.ICyberEntity
import net.liplum.api.cyber.SideLinks.Companion.reflect
import net.liplum.data.PayloadDataList
import net.liplum.lib.Out
import net.liplum.lib.Serialized
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.*
import plumy.pathkt.IVertex

interface INetworkNode : ICyberEntity, IVertex<INetworkNode> {
    @Serialized
    var network: DataNetwork
    var init: Boolean
    var links: SideLinks
    @Serialized
    val dataList: PayloadDataList
    @Serialized
    val currentOriented: Pos
    /**
     * [0f,1f]
     */
    @Serialized
    val sendingProgress: Float
    var routine: DataNetwork.Path?
    val sideEnable: SideEnable

    companion object {
        private val tempList = ArrayList<INetworkNode>()
    }

    fun canTransferTo(other: INetworkNode): Boolean =
        routine != null && routine == other.routine

    override val linkedVertices: Iterable<INetworkNode>
        get() = getNetworkConnections(tempList)

    fun isLinkedWith(side: Side, other: INetworkNode) =
        other.building.pos() == links[side]

    fun isLinkedWith(other: INetworkNode) =
        other.building.pos() in links
    /**
     * @return [out]
     */
    fun getNetworkConnections(@Out out: MutableList<INetworkNode>):
            MutableList<INetworkNode> {
        out.clear()
        links.forEachNode { out.add(it) }
        return out
    }
    /**
     * Call this function when self is removed from the ground.
     * It will correct the network
     */
    fun onRemoveFromGround() {
        links.forEachSide { side ->
            // If this is a node existed on this side, separate the network
            clearSide(side)
        }
    }

    fun isSideFull(side: Side) =
        links[side] != -1
    @ClientOnly
    val expendSelectingLineTime: Float
    val linkRange: WorldXY
    val tileLinkRange: TileXY
        get() = (linkRange / Vars.tilesize).toInt()
    /**
     * It will merge [target] into self's network.
     * It will clear the old sides of each other
     * @param side relative to self's side
     */
    fun link(side: Side, target: INetworkNode) {
        // clear the old one
        clearSide(side)
        target.clearSide(side.reflect)

        this.links[side] = target
        target.links[side.reflect] = this
        this.network.merge(target)
    }
    /**
     * It takes the side into account.
     * @param side relative to self's side
     */
    fun canLink(side: Side, target: INetworkNode): Boolean {
        if (target.building.dst(building) > linkRange) return false
        val thisOld = this.links[side]
        val targetOld = target.links[side.reflect]
        if (thisOld != -1 && targetOld != -1) return false
        return true
    }
    /**
     * Clear this side and reflow the network.
     * If the side already held a node, reset its [INetworkNode.links] and create a new network for it.
     * it doesn't consider the enabled side
     */
    fun clearSide(side: Side) {
        val old = links[side]
        if (old == -1) return
        links[side] = -1
        old.TEAny<INetworkNode>()?.let {
            // If this is a node existed on this side, separate the network
            it.links[side.reflect] = -1
            if (it.network == this.network) {
                val newNetwork = DataNetwork()
                it.network.remove()
                it.network = newNetwork
                newNetwork.reflow(it)
            }
        }
        this.network.reflow(this)
    }
}
/**
 * Only iterate the enabled sides
 */
inline fun INetworkNode.forEachEnabledSide(func: (Side) -> Unit) {
    for (side in RIGHT..BOTTOM) {
        if (sideEnable[side])
            func(side)
    }
}

interface INetworkBlock {
    val linkRange: WorldXY
    val tileLinkRange: TileXY
        get() = (linkRange / Vars.tilesize).toInt()
    val block: Block
    val dataCapacity: Int
    @ClientOnly
    val expendPlacingLineTime: Float
    val sideEnable: SideEnable
    fun initNetworkNodeSettings() {
        block.apply {
            configurable = true
        }
    }
}
/**
 * Only iterate the enabled sides
 */
inline fun INetworkBlock.forEachEnabledSide(func: (Side) -> Unit) {
    for (side in RIGHT..BOTTOM) {
        if (sideEnable[side])
            func(side)
    }
}

object EmptyNetworkNode : INetworkNode {
    override var network = DataNetwork()
    override var init = true
    override var links = SideLinks()
    override val dataList: PayloadDataList = PayloadDataList()
    @ClientOnly
    override val expendSelectingLineTime = 0f
    override val currentOriented: Pos = NewEmptyPos()
    override val sendingProgress: Float = 0f
    override var routine: DataNetwork.Path? = DataNetwork.Path()
    override val sideEnable = SideEnable(4) { false }
    override val linkRange = 0f
}
