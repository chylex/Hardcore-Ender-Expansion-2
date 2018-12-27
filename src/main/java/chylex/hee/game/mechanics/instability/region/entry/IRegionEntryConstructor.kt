package chylex.hee.game.mechanics.instability.region.entry
import net.minecraft.util.math.BlockPos

interface IRegionEntryConstructor<T : IRegionEntry>{
	fun fromCompacted(compacted: Long): T
	fun fromPos(pos: BlockPos): T
}
