package org.example.cn.pprocket.utils.app

import com.github.unidbg.AndroidEmulator
import com.github.unidbg.Symbol
import com.github.unidbg.linux.android.AndroidEmulatorBuilder
import com.github.unidbg.linux.android.AndroidResolver
import com.github.unidbg.linux.android.dvm.*
import com.github.unidbg.linux.android.dvm.api.Signature
import com.github.unidbg.linux.android.dvm.array.ArrayObject
import java.io.File

object AppSignGenerator :AbstractJni() {
    private var symbol: Symbol? = null
    private var emulator: AndroidEmulator? = null
    private var vm: VM? = null
    private var flag = false
    fun hkey(path: String, time: String, nonce: String): String {
        val args: MutableList<Any> = ArrayList(10)
        args.add(vm!!.jniEnv)
        args.add(0)
        args.add(vm!!.addLocalObject(vm!!.resolveClass("android.content.Context").newObject(null)))
        args.add(vm!!.addLocalObject(StringObject(vm, path)))
        args.add(vm!!.addLocalObject(StringObject(vm, time)))
        args.add(vm!!.addLocalObject(StringObject(vm, nonce)))

        val start = System.currentTimeMillis()
        val number = symbol!!.call(emulator, *args.toTypedArray())
        val result = vm!!.getObject<DvmObject<*>>(number.toInt()).value.toString()

        println("Sign took ${System.currentTimeMillis() - start} ms")

        return result
    }

    init {
        emulator = AndroidEmulatorBuilder.for64Bit()
            .setProcessName("com.qidian.dldl.official")
            .build() // 创建模拟器实例，要模拟32位或者64位，在这里区分
        val memory = emulator!!.memory // 模拟器的内存操作接口
        memory.setLibraryResolver(AndroidResolver(23)) // 设置系统类库解析

        vm = emulator!!.createDalvikVM(File("heybox.apk"))

        vm!!.setVerbose(false)
        vm!!.setJni(this)
        val dm = vm!!.loadLibrary(File("libnative-lib.so"), true)
        val module = dm.module

        symbol = module.findSymbolByName("Java_com_starlightc_ucropplus_network_temp_TempEncodeUtil_encode")

    }
    override fun callObjectMethod(
        vm: BaseVM?,
        dvmObject: DvmObject<*>?,
        signature: String?,
        varArg: VarArg?
    ): DvmObject<*> {
        if (signature == "android/content/Context->getPackageName()Ljava/lang/String;") {
            return (StringObject(vm!!, "com.max.xiaoheihe"))
        }

        if (signature == "android/content/pm/PackageInfo->signatures:[Landroid/content/pm/Signature;") {
            return (StringObject(vm!!, ""))
        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg)
    }

    override fun getObjectField(vm: BaseVM?, dvmObject: DvmObject<*>?, signature: String?): DvmObject<*> {
        if (signature == "android/content/pm/PackageInfo->signatures:[Landroid/content/pm/Signature;") {
            var signature1 = Signature(vm!!,null)
            return ArrayObject(signature1)
        }
        return super.getObjectField(vm, dvmObject, signature)
    }

    override fun callIntMethod(vm: BaseVM?, dvmObject: DvmObject<*>?, signature: String?, varArg: VarArg?): Int {
        if (signature == "android/content/pm/Signature->hashCode()I") {
            return 67780190
        }
        return super.callIntMethod(vm, dvmObject, signature, varArg)
    }
}
