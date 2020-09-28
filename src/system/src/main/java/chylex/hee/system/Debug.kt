package chylex.hee.system
import chylex.hee.client.MC
import chylex.hee.client.render.gl.DF_ONE
import chylex.hee.client.render.gl.DF_ZERO
import chylex.hee.client.render.gl.RenderStateBuilder
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.LAYERING_PROJECTION
import chylex.hee.client.render.gl.RenderStateBuilder.Companion.MASK_COLOR
import chylex.hee.client.render.gl.SF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.gl.SF_SRC_ALPHA
import chylex.hee.game.inventory.nbtOrNull
import chylex.hee.game.world.floodFill
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getState
import chylex.hee.game.world.isAir
import chylex.hee.game.world.removeBlock
import chylex.hee.game.world.setState
import chylex.hee.proxy.Environment
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.ItemBlock
import chylex.hee.system.serialization.hasKey
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.Items
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.UP
import net.minecraft.util.Direction.WEST
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.Hand.OFF_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.world.IWorld
import net.minecraftforge.client.event.DrawHighlightEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.InputEvent.KeyInputEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock
import net.minecraftforge.event.world.BlockEvent.BreakEvent
import org.apache.commons.lang3.SystemUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import java.io.File
import java.lang.management.ManagementFactory

object Debug{
	val enabled = System.getProperty("hee.debug") != null
	
	@Sided(Side.CLIENT)
	fun initializeClient(){
		if (enabled){
			MinecraftForge.EVENT_BUS.register(object : Any(){
				@SubscribeEvent
				fun onKeyPressed(e: KeyInputEvent){
					if (e.action != GLFW.GLFW_PRESS){
						return
					}
					
					if (e.key == GLFW.GLFW_KEY_GRAVE_ACCENT){
						val player = MC.player ?: return
						
						if (player.isCreative){
							val ctrl = (e.modifiers and GLFW.GLFW_MOD_CONTROL) != 0
							player.sendChatMessage(if (ctrl) "/gamemode spectator" else "/gamemode survival")
						}
						else{
							player.sendChatMessage("/gamemode creative")
						}
					}
					else if (e.key == GLFW.GLFW_KEY_RIGHT_ALT){
						forceCancelCtrl = true
					}
					else if (e.key == GLFW.GLFW_KEY_LEFT_CONTROL){
						forceCancelCtrl = false
					}
				}
				
				private fun isHoldingBuildStick(player: EntityPlayer): Boolean{
					val heldItem = player.getHeldItem(MAIN_HAND)
					return heldItem.item === Items.STICK && heldItem.nbtOrNull.hasKey("HEE_BUILD")
				}
				
				private fun getBuildStickBlocks(world: IWorld, pos: BlockPos, state: BlockState, face: Direction): List<BlockPos>{
					val floodFaces = when(face){
						UP, DOWN     -> listOf(NORTH, SOUTH, EAST, WEST)
						NORTH, SOUTH -> listOf(UP, DOWN, EAST, WEST)
						EAST, WEST   -> listOf(UP, DOWN, NORTH, SOUTH)
						else         -> emptyList()
					}
					
					val limit = 1000
					val block = state.block
					return pos.floodFill(floodFaces, limit){ it.getBlock(world) === block }.takeIf { it.size < limit }.orEmpty()
				}
				
				private var lastLeftClickHit: BlockRayTraceResult? = null
				
				@SubscribeEvent
				fun onLeftClickBlock(e: LeftClickBlock){
					val world = e.world
					
					if (isHoldingBuildStick(e.player) && !world.isRemote){
						lastLeftClickHit = MC.instance.objectMouseOver as? BlockRayTraceResult
					}
				}
				
				@SubscribeEvent
				fun onBlockBreak(e: BreakEvent){
					val world = e.world
					
					if (isHoldingBuildStick(e.player) && !world.isRemote){
						val hit = lastLeftClickHit ?: return
						
						for(pos in getBuildStickBlocks(world, e.pos, e.state, hit.face)){
							pos.removeBlock(world)
						}
					}
				}
				
				@SubscribeEvent
				fun onRightClickBlock(e: RightClickBlock){
					val world = e.world
					val player = e.player
					
					if (isHoldingBuildStick(player) && !world.isRemote){
						val state = e.pos.getState(world)
						val face = e.face!!
						
						val place = (player.getHeldItem(OFF_HAND).item as? ItemBlock)?.block?.defaultState ?: state
						
						for(pos in getBuildStickBlocks(world, e.pos, state, face)){
							val offset = pos.offset(face)
							
							if (offset.isAir(world)){
								offset.setState(world, place)
							}
						}
					}
				}
				
				private val RENDER_TYPE_LINE = with(RenderStateBuilder()){
					line(2.25)
					blend(SF_SRC_ALPHA, DF_ONE, SF_ONE_MINUS_SRC_ALPHA, DF_ZERO)
					layering(LAYERING_PROJECTION)
					mask(MASK_COLOR)
					buildType("hee:debug_line", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, bufferSize = 256)
				}
				
				@SubscribeEvent
				fun onRenderOverlay(e: DrawHighlightEvent.HighlightBlock){
					val player = MC.player!!
					
					if (isHoldingBuildStick(player)){
						val hit = MC.instance.objectMouseOver as? BlockRayTraceResult ?: return
						val world = player.world
						val center = hit.pos
						val info = e.info
						
						val matrix = e.matrix.last.matrix
						val builder = e.buffers.getBuffer(RENDER_TYPE_LINE)
						
						for(pos in getBuildStickBlocks(world, center, center.getState(world), hit.face)){
							val x = pos.x - info.projectedView.x
							val y = pos.y - info.projectedView.y
							val z = pos.z - info.projectedView.z
							
							val shape = pos.getState(world).getShape(world, pos, ISelectionContext.forEntity(info.renderViewEntity))
							
							shape.forEachEdge { x1, y1, z1, x2, y2, z2 ->
								builder.pos(matrix, (x1 + x).toFloat(), (y1 + y).toFloat(), (z1 + z).toFloat()).color(1F, 1F, 1F, 1F).endVertex()
								builder.pos(matrix, (x2 + x).toFloat(), (y2 + y).toFloat(), (z2 + z).toFloat()).color(1F, 1F, 1F, 1F).endVertex()
							}
						}
						
						e.isCanceled = true
					}
				}
			})
			
			if (canExecutePowershell("maximize.ps1")){
				MinecraftForge.EVENT_BUS.register(object : Any(){
					@SubscribeEvent
					fun onGuiOpen(@Suppress("UNUSED_PARAMETER") e: GuiOpenEvent){
						val pid = ManagementFactory.getRuntimeMXBean().name.split("@")[0]
						ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Unrestricted", "-File", "maximize.ps1", pid).start()
						
						MinecraftForge.EVENT_BUS.unregister(this)
					}
				})
			}
		}
	}
	
	private var forceCancelCtrl = false
	
	@JvmStatic
	@Suppress("unused")
	fun cancelControlKey(): Boolean{
		return forceCancelCtrl
	}
	
	fun setClipboardContents(file: File){
		if (canExecutePowershell("filecopy.ps1")){
			ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Unrestricted", "-Sta", "-File", "filecopy.ps1", file.absolutePath).start()
		}
	}
	
	private fun canExecutePowershell(scriptName: String): Boolean{
		return SystemUtils.IS_OS_WINDOWS && Environment.side == Side.CLIENT && File(scriptName).exists()
	}
}
