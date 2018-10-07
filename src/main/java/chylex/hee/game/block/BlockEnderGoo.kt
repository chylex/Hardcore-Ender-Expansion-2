package chylex.hee.game.block
import chylex.hee.game.block.material.Materials
import chylex.hee.game.item.util.BlockEditor
import chylex.hee.game.render.util.GL
import chylex.hee.game.render.util.RGB
import chylex.hee.init.ModItems
import chylex.hee.system.Resource
import chylex.hee.system.util.FLAG_NOTIFY_NEIGHBORS
import chylex.hee.system.util.FLAG_RENDER_IMMEDIATE
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.getState
import chylex.hee.system.util.setBlock
import net.minecraft.block.material.MapColor
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.GlStateManager.FogMode.EXP
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.fluids.BlockFluidClassic
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class BlockEnderGoo : BlockFluidClassic(FLUID, Materials.ENDER_GOO){
	companion object{
		val COLOR = RGB(136, 26, 190)
		private val COLOR_VEC = COLOR.let { (r, g, b) -> Vec3d(r / 255.0, g / 255.0, b / 255.0) }
		
		val FLUID = Fluid("ender_goo", Resource.Custom("block/ender_goo_still"), Resource.Custom("block/ender_goo_flowing")).apply {
			density = 1500
			viscosity = 1500
			temperature = 233
		}
	}
	
	init{
		enableStats = false
		
		tickRate = 18
		quantaPerBlock = 5
		
		FLUID.block = this
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onFillBucket(e: FillBucketEvent){
		val target = e.target
		
		if (target == null || target.typeOfHit != BLOCK){
			return
		}
		
		val world = e.world
		val player = e.entityPlayer
		val pos = target.blockPos
		
		if (!BlockEditor.canEdit(pos, e.entityPlayer, e.emptyBucket) || !world.isBlockModifiable(player, pos)){
			return
		}
		
		val state = pos.getState(world)
		
		if (state.block != this || state.getValue(LEVEL) != 0){
			return
		}
		
		player.addStat(StatList.getObjectUseStats(e.emptyBucket.item)!!)
		// TODO sound effect?
		
		pos.setBlock(world, Blocks.AIR, FLAG_NOTIFY_NEIGHBORS or FLAG_SYNC_CLIENT or FLAG_RENDER_IMMEDIATE)
		e.filledBucket = ItemStack(ModItems.ENDER_GOO_BUCKET)
		e.result = ALLOW
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	fun onFogDensity(e: FogDensity){
		val entity = e.entity
		val insideOf = ActiveRenderInfo.getBlockStateAtEntityViewpoint(entity.world, entity, e.renderPartialTicks.toFloat())
		
		if (insideOf.material === Materials.ENDER_GOO){
			GL.setFog(EXP)
			e.density = 0.66F
			e.isCanceled = true // otherwise the event is ignored
		}
	}
	
	@SideOnly(Side.CLIENT)
	override fun getFogColor(world: World, pos: BlockPos, state: IBlockState, entity: Entity, originalColor: Vec3d, partialTicks: Float): Vec3d{
		return COLOR_VEC
	}
	
	override fun getMapColor(state: IBlockState, world: IBlockAccess, pos: BlockPos): MapColor{
		return MapColor.PURPLE
	}
}
