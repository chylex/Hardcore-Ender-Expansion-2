package chylex.hee.game.block
import chylex.hee.game.block.BlockGraveDirt.Type.LOOT
import chylex.hee.game.block.BlockGraveDirt.Type.PLAIN
import chylex.hee.game.block.BlockGraveDirt.Type.SPIDERLING
import chylex.hee.game.block.util.Property
import chylex.hee.init.ModLoot
import chylex.hee.system.util.center
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.toYaw
import chylex.hee.system.util.with
import net.minecraft.block.BlockDirt
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.monster.EntityCaveSpider
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IStringSerializable
import net.minecraft.util.NonNullList
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.Random

class BlockGraveDirt(builder: BlockSimple.Builder) : BlockSimpleShaped(builder, AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0)){
	companion object{
		val TYPE = Property.enum<Type>("type")
		val FULL = Property.bool("full")
		
		private fun makeSpiderling(world: World, pos: BlockPos, yaw: Float): EntityCaveSpider{
			return EntityCaveSpider(world).apply { // TODO
				setLocationAndAngles(pos.x + 0.5, pos.y + 0.01, pos.z + 0.5, yaw, 0F)
			}
		}
	}
	
	enum class Type(private val serializableName: String) : IStringSerializable{
		PLAIN("plain"),
		SPIDERLING("spiderling"),
		LOOT("loot");
		
		override fun getName(): String{
			return serializableName
		}
	}
	
	// Instance
	
	init{
		defaultState = blockState.baseState.with(TYPE, PLAIN).with(FULL, true)
	}
	
	override fun createBlockState() = BlockStateContainer(this, TYPE, FULL)
	
	override fun getMetaFromState(state: IBlockState) = state[TYPE].ordinal
	override fun getStateFromMeta(meta: Int) = this.with(TYPE, Type.values()[meta])
	
	// Bounding box
	
	override fun getActualState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState{
		return state.with(FULL, pos.up().getBlock(world) === this)
	}
	
	override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB{
		return if (state.getActualState(source, pos)[FULL]) FULL_BLOCK_AABB else super.getBoundingBox(state, source, pos)
	}
	
	// Drops
	
	override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item{
		return Item.getItemFromBlock(Blocks.DIRT)
	}
	
	override fun damageDropped(state: IBlockState): Int{
		return BlockDirt.DirtType.COARSE_DIRT.metadata
	}
	
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		super.getDrops(drops, world, pos, state, fortune)
		
		if (state[TYPE] == LOOT){
			ModLoot.GRAVE_DIRT_LOOT.generateDrops(drops, world, fortune) // TODO include modded nuggets
		}
	}
	
	override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, tile: TileEntity?, stack: ItemStack){
		super.harvestBlock(world, player, pos, state, tile, stack)
		
		if (state[TYPE] == SPIDERLING){
			makeSpiderling(world, pos, player.posVec.subtract(pos.center).toYaw()).apply {
				world.spawnEntity(this)
				attackTarget = player
			}
		}
	}
	
	override fun canSilkHarvest() = false
	
	// Explosions
	
	override fun canDropFromExplosion(explosion: Explosion): Boolean{
		return false
	}
	
	override fun onBlockExploded(world: World, pos: BlockPos, explosion: Explosion){
		val state = pos.getState(world)
		val rand = world.rand
		
		super.onBlockExploded(world, pos, explosion)
		
		if (!world.isRemote && rand.nextInt(5) == 0){
			spawnAsEntity(world, pos, ItemStack(getItemDropped(state, rand, 0), quantityDropped(rand), damageDropped(state)))
			dropBlockAsItem(world, pos, state, 0)
		}
		
		if (state[TYPE] == SPIDERLING){
			if (world.isRemote){
				makeSpiderling(world, pos, yaw = 0F).spawnExplosionParticle()
				SoundEvents.ENTITY_SPIDER_DEATH.playClient(pos, SoundCategory.HOSTILE, volume = 0.8F, pitch = rand.nextFloat(0.9F, 1F)) // TODO update sound
			}
			else if (world.gameRules.getBoolean("doMobLoot")){
				spawnAsEntity(world, pos, ItemStack(Items.STRING))
				// TODO loot table
			}
		}
	}
}
