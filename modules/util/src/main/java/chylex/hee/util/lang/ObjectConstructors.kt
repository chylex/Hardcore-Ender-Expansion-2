package chylex.hee.util.lang

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.function.Function
import java.util.function.Supplier

object ObjectConstructors {
	@Suppress("UNCHECKED_CAST")
	inline fun <reified T> noArgs(): Supplier<T> {
		val mh = MethodHandles.lookup()
		val con = mh.unreflectConstructor(T::class.java.getConstructor())
		
		val conType = con.type()
		val samType = conType.generic()
		val retType = MethodType.methodType(Supplier::class.java)
		
		return LambdaMetafactory.metafactory(mh, "get", retType, samType, con, conType).target.invokeExact() as Supplier<T>
	}
	
	@Suppress("UNCHECKED_CAST")
	fun <T, U> oneArg(constructedType: Class<T>, parameterType: Class<U>): Function<U, T> {
		val mh = MethodHandles.lookup()
		val con = mh.unreflectConstructor(constructedType.getConstructor(parameterType))
		
		val conType = con.type()
		val samType = conType.generic()
		val retType = MethodType.methodType(Function::class.java)
		
		return LambdaMetafactory.metafactory(mh, "apply", retType, samType, con, conType).target.invokeExact() as Function<U, T>
	}
	
	@Suppress("UNCHECKED_CAST")
	inline fun <reified ConstructedType : ParentType, reified ParentType, reified FactoryType> generic(constructMethodName: String, vararg constructMethodArgs: Class<*>): MethodHandle {
		return generic(ConstructedType::class.java, ParentType::class.java, FactoryType::class.java, constructMethodName, *constructMethodArgs)
	}
	
	@Suppress("UNCHECKED_CAST")
	fun <ConstructedType : ParentType, ParentType, FactoryType> generic(constructedType: Class<ConstructedType>, parentType: Class<ParentType>, factoryType: Class<FactoryType>, constructMethodName: String, vararg constructMethodArgs: Class<*>): MethodHandle {
		val mh = MethodHandles.lookup()
		val con = mh.unreflectConstructor(constructedType.getConstructor(*constructMethodArgs))
		
		val conType = con.type()
		val samType = conType.changeReturnType(parentType)
		val retType = MethodType.methodType(factoryType)
		
		return LambdaMetafactory.metafactory(mh, constructMethodName, retType, samType, con, conType).target
	}
}
