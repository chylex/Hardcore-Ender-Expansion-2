package chylex.hee.game.block

import chylex.hee.game.block.builder.HeeBlockComponents
import chylex.hee.game.world.util.getBlock
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.forge.asBool
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.state.StateContainer.Builder
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Direction
import net.minecraft.util.Hand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

abstract class HeeBlockWithComponents(properties: Properties, components: HeeBlockComponents) : HeeBlock2(properties) {
	private companion object {
		private val RAND = Random()
	}
	
	private val states = components.states.build()
	
	private val name = components.name
	private val tooltip = components.tooltip
	
	private val shape = components.shape
	private val renderType = components.renderType
	private val ambientOcclusionValue = components.ambientOcclusionValue
	private val clientEffects = components.clientEffects
	
	private val drops = components.drops
	private val harvestability = components.harvestability
	private val experience = components.experience
	private val flammability = components.flammability
	
	private val entity = components.entity
	private val placement = components.placement
	private val onAdded = components.onAdded
	private val onNeighborChanged = components.onNeighborChanged
	private val setStateFromNeighbor = components.setStateFromNeighbor
	
	private val scheduledTick = components.scheduledTick
	private val randomTick = components.randomTick
	
	private val playerUse = components.playerUse
	private val onExploded = components.onExploded
	private val onCreatureSpawning = components.onCreatureSpawning
	private val collideWithEntity = components.collideWithEntity
	
	private var isAir = components.isAir
	
	init {
		defaultState = states.applyDefaults(stateContainer.baseState)
	}
	
	abstract override fun fillStateContainer(builder: Builder<Block, BlockState>)
	
	override fun rotate(state: BlockState, rotation: Rotation): BlockState {
		return states.getRotatedState(state, rotation)
	}
	
	override fun mirror(state: BlockState, mirror: Mirror): BlockState {
		return states.getMirroredState(state, mirror)
	}
	
	override fun getTranslationKey(): String {
		return name?.translationKey ?: super.getTranslationKey()
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: IBlockReader?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		tooltip?.add(lines, stack, flags.isAdvanced, world)
	}
	
	@Suppress("DEPRECATION")
	override fun getShape(state: BlockState, worldIn: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		return shape?.getShape(state) ?: super.getShape(state, worldIn, pos, context)
	}
	
	@Suppress("DEPRECATION")
	override fun getCollisionShape(state: BlockState, worldIn: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		return shape?.getCollisionShape(state) ?: super.getCollisionShape(state, worldIn, pos, context)
	}
	
	override fun getRaytraceShape(state: BlockState, worldIn: IBlockReader, pos: BlockPos): VoxelShape {
		return super.getRaytraceShape(state, worldIn, pos)
	}
	
	@Suppress("DEPRECATION")
	override fun getRenderType(state: BlockState): BlockRenderType {
		return renderType ?: super.getRenderType(state)
	}
	
	@Sided(Side.CLIENT)
	@Suppress("DEPRECATION")
	override fun getAmbientOcclusionLightValue(state: BlockState, worldIn: IBlockReader, pos: BlockPos): Float {
		return ambientOcclusionValue ?: super.getAmbientOcclusionLightValue(state, worldIn, pos)
	}
	
	@Sided(Side.CLIENT)
	override fun animateTick(state: BlockState, world: World, pos: BlockPos, rand: Random) {
		clientEffects?.randomTick(state, world, pos, rand)
	}
	
	@Suppress("DEPRECATION")
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack> {
		return drops?.getDrops(state, context) ?: super.getDrops(state, context)
	}
	
	override fun getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: PlayerEntity): ItemStack {
		return drops?.getPickBlock(state, world, pos) ?: super.getPickBlock(state, target, world, pos, player)
	}
	
	override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: PlayerEntity): Boolean {
		return harvestability?.canHarvest(player)?.asBool ?: super.canHarvestBlock(state, world, pos, player)
	}
	
	override fun getExpDrop(state: BlockState, world: IWorldReader, pos: BlockPos, fortune: Int, silktouch: Int): Int {
		return experience?.getExperience((world as? World)?.rand ?: RAND) ?: 0
	}
	
	override fun getFlammability(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return flammability?.takeIfFlammable(state)?.flammability ?: 0
	}
	
	override fun getFireSpreadSpeed(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return flammability?.takeIfFlammable(state)?.fireSpread ?: 0
	}
	
	override fun hasTileEntity(state: BlockState): Boolean {
		return entity != null
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity? {
		return entity?.create()
	}
	
	@Suppress("DEPRECATION")
	override fun isValidPosition(state: BlockState, world: IWorldReader, pos: BlockPos): Boolean {
		return placement?.isPositionValid(state, world, pos) ?: super.isValidPosition(state, world, pos)
	}
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState? {
		return placement?.getPlacedState(defaultState, context.world, context.pos, context) ?: super.getStateForPlacement(context)
	}
	
	override fun onBlockPlacedBy(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
		placement?.onPlacedBy(state, world, pos, placer, stack)
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
		onAdded?.onAdded(state, world, pos)
		scheduledTick?.onAdded(state, world, pos, world.rand)
	}
	
	@Suppress("DEPRECATION")
	override fun neighborChanged(state: BlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos, isMoving: Boolean) {
		super.neighborChanged(state, world, pos, neighborBlock, neighborPos, isMoving)
		onNeighborChanged?.onNeighborChanged(state, world, pos, neighborBlock, neighborPos.getBlock(world), neighborPos)
	}
	
	@Suppress("DEPRECATION")
	override fun updatePostPlacement(state: BlockState, neighborFacing: Direction, neighborState: BlockState, world: IWorld, currentPos: BlockPos, neighborPos: BlockPos): BlockState {
		return setStateFromNeighbor?.getNewState(state, world, currentPos, neighborFacing, neighborPos) ?: super.updatePostPlacement(state, neighborFacing, neighborState, world, currentPos, neighborPos)
	}
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {
		scheduledTick?.onTick(state, world, pos, rand)
	}
	
	override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {
		randomTick?.onTick(state, world, pos, rand)
	}
	
	@Suppress("DEPRECATION")
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
		return playerUse?.use(state, world, pos, player, hand) ?: super.onBlockActivated(state, world, pos, player, hand, hit)
	}
	
	override fun canDropFromExplosion(state: BlockState, world: IBlockReader, pos: BlockPos, explosion: Explosion): Boolean {
		return onExploded?.canDrop(explosion) ?: super.canDropFromExplosion(state, world, pos, explosion)
	}
	
	override fun onBlockExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion) {
		onExploded?.onExploded(state, world, pos, explosion)
		super.onBlockExploded(state, world, pos, explosion)
	}
	
	override fun canCreatureSpawn(state: BlockState, world: IBlockReader, pos: BlockPos, type: PlacementType?, entityType: EntityType<*>?): Boolean {
		return onCreatureSpawning?.canSpawn(world, pos, type, entityType)?.asBool ?: super.canCreatureSpawn(state, world, pos, type, entityType)
	}
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
		collideWithEntity?.collide(state, world, pos, entity)
	}
	
	override fun isAir(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean {
		return isAir ?: super.isAir(state, world, pos)
	}
	
	override fun propagatesSkylightDown(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean {
		return isAir == true || super.propagatesSkylightDown(state, world, pos)
	}
}
