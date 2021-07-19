package chylex.hee.game.block.properties

import net.minecraft.block.Block
import net.minecraft.util.ResourceLocation

sealed class BlockStatePreset {
	object None : BlockStatePreset()
	object Simple : BlockStatePreset()
	class SimpleFrom(val modelBlock: Block) : BlockStatePreset()
	object Pillar : BlockStatePreset()
	class PillarFrom(val modelBlock: Block) : BlockStatePreset()
	class Stairs(val fullBlock: Block, val side: ResourceLocation? = null) : BlockStatePreset()
	class Slab(val fullBlock: Block, val side: ResourceLocation? = null) : BlockStatePreset()
	class Wall(val fullBlock: Block) : BlockStatePreset()
	class Cauldron(val fluidTexture: ResourceLocation) : BlockStatePreset()
	object Log : BlockStatePreset()
}
