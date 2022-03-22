package com.example.coroutinestest

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.test).setOnClickListener {
            initView()
        }
    }

    private fun initView() {
//        loadData()
        loadData0()
//        loadData1()
//        loadData2()
//        loadData3()
//        loadData4()
//        loadData5()


//        coroutineBuildRunBlock7()
//        coroutineBuildRunBlock8()
//        coroutineBuildRunBlock9()
//        coroutineBuildRunBlock10()

    }


    /**
     * 不会崩溃
     */

    private val job: Job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private fun doWork(): Deferred<String> = scope.async { throw NullPointerException("自定义空指针异常") }


    private fun loadData() = scope.launch {
        try {
            doWork().await()
        } catch (e: Exception) {
            Log.d("try catch捕获的异常:", e.toString())
        }
    }

    /**
     * 会崩溃
     *
     *2022-03-22 19:51:02.074 25864-25903/com.example.coroutinestest D/async 异常:: 开始准备抛出异常
    2022-03-22 19:51:03.085 25864-25905/com.example.coroutinestest D/async 异常: 捕获的异常-: java.lang.NullPointerException: 自定义空指针异常
    2022-03-22 19:51:03.085 25864-25905/com.example.coroutinestest D/async 异常:: 继续执行后续代码

    虽然捕获到了异常但是依然引起了崩溃，这里的崩溃其实不是调用await引起的而是执行到抛出异常那里就引起了，只是崩溃后这个异常被捕获到了而已

     *
    当async作为根协程时，被封装到deferred对象中的异常才会在调用await时抛出。
    如果async作为一个子协程时，那么异常并不会等到调用await时抛出，而是立刻抛出异常。
     *
     */

    private val job0: Job = Job()
    private val scope0 = CoroutineScope(Dispatchers.Default + job0)

//    private fun loadData0() = scope0.launch {
//        val asy = async {
//            Log.d("async 异常:", "开始准备抛出异常")
//            delay(1000)
//            throw NullPointerException("自定义空指针异常")
//        }
//        try {
//            //崩溃原因其实不是在调用await方法之后引起的崩溃，是代码执行到 throw NullPointerException("自定义空指针异常")就抛出异常了，可以IP屏蔽掉asy.await()方法看日志就知道
//            //2022-03-22 19:55:05.460 26378-26415/com.example.coroutinestest D/async 异常:: 继续执行后续代码
//            //2022-03-22 19:55:05.461 26378-26415/com.example.coroutinestest D/async 异常:: 开始准备抛出异常
//
//            asy.await()
//        } catch (e: Exception) {
//            Log.d("async 异常: 捕获的异常-", e.toString())
//        }
//        Log.d("async 异常:", "继续执行后续代码")
//    }

    //解决上诉问题的方法就是这个异常被内部coroutineexceptionhandler捕获并处理，像下面这样
//    2022-03-22 20:02:31.121 27083-27166/com.example.coroutinestest D/async 异常:: 开始准备抛出异常
//    2022-03-22 20:02:32.134 27083-27167/com.example.coroutinestest D/async 异常: 捕获的异常-: java.lang.NullPointerException: 自定义空指针异常
//    2022-03-22 20:02:32.134 27083-27167/com.example.coroutinestest D/async 异常:: 继续执行后续代码
//    2022-03-22 20:02:32.135 27083-27166/com.example.coroutinestest D/async 异常:: 异常被内部CoroutineExceptionHandler处理掉了

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
        Log.d("async 异常:", "异常被内部CoroutineExceptionHandler处理掉了")
    }

    private fun loadData0() = scope0.launch(coroutineExceptionHandler) {
        val asy = async {
            Log.d("async 异常:", "开始准备抛出异常")
            delay(1000)
            throw NullPointerException("自定义空指针异常")
        }
        try {
            asy.await()
        } catch (e: Exception) {
            Log.d("async 异常: 捕获的异常-", e.toString())
        }
        Log.d("async 异常:", "继续执行后续代码")
    }






    /**
     * 会崩溃，因为launch开启的协程是不会向外线程牌抛出异常的，所以try catch是不会捕获到当前线程的异常的所以会崩溃
     */

    private val job1: Job = Job()
    private val scope1 = CoroutineScope(Dispatchers.Default + job1)

    private fun doWork1() = scope1.launch { throw NullPointerException("自定义空指针异常") }


    private fun loadData1() = scope1.launch {
        try {
            doWork1()
        } catch (e: Exception) {
            Log.d("try catch捕获的异常:", e.toString())
        }
    }


    /**
     * 不会崩溃，
     */

    private val job2: Job = Job()
    private val scope2 = CoroutineScope(Dispatchers.Default + job2)

    private fun loadData2() = scope2.launch(CoroutineExceptionHandler { _, exception ->
        {
            Log.d("Handler捕获的异常", exception.toString())
        }
    }) {
        try {
            //无论launch有几层都不会崩溃
            launch { launch { throw NullPointerException("自定义空指针异常") } }
        } catch (e: Exception) {
            Log.d("try catch捕获的异常:", e.toString())
        }
    }


    /**
     * 会崩溃，
     */
    private val job3: Job = Job()
    private val scope3 = CoroutineScope(Dispatchers.Default + job3)

    private fun doWork3() = scope3.launch { throw NullPointerException("自定义空指针异常") }

    private fun loadData3() = scope3.launch(CoroutineExceptionHandler { _, exception ->
        {
            Log.d("Handler捕获的异常", exception.toString())
        }
    }) {
        try {
            doWork3()
        } catch (e: Exception) {
            Log.d("try catch捕获的异常:", e.toString())
        }
    }


    /**
     * 不会崩溃，
     */
    private val job4: Job = Job()
    private val scope4 =
        CoroutineScope(Dispatchers.Default + job4 + CoroutineExceptionHandler { _, exception ->
            {
                Log.d("Handler捕获的异常", exception.toString())
            }
        })

    //无论launch有几层都不会崩溃
    private fun doWork4() = scope4.launch { launch { throw NullPointerException("自定义空指针异常") } }

    private fun loadData4() = scope4.launch() {
        try {
            doWork4()
        } catch (e: Exception) {
            Log.d("try catch捕获的异常:", e.toString())
        }
    }


    /**
     * 不会崩溃，
     */
    private val job5: Job = Job()
    private val scope5 =
        CoroutineScope(Dispatchers.Default + job5 + CoroutineExceptionHandler { _, exception ->
            {
                Log.d("Handler捕获的异常", exception.toString())
            }
        })
    private val scope5x =
        CoroutineScope(Dispatchers.Default + job5 + CoroutineExceptionHandler { _, exception ->
            {
                Log.d("Handler捕获的异常", exception.toString())
            }
        })

    //无论launch有几层都不会崩溃
    private fun doWork5() = scope5x.launch { launch { throw NullPointerException("自定义空指针异常") } }

    private fun loadData5() = scope5.launch() {
        try {
            doWork5()
        } catch (e: Exception) {
            Log.d("try catch捕获的异常:", e.toString())
        }
    }


//=============================================supervisorScope 和 SupervisorJob//=============================================supervisorScope 和 SupervisorJob//=============================================


    /**
     * 不会崩溃
     *
    2022-03-22 15:24:34.022 20373-20411/com.example.coroutinestest D/kobe: start job1 delay
    2022-03-22 15:24:34.025 20373-20412/com.example.coroutinestest D/kobe: job2 throw execption
    2022-03-22 15:24:34.029 20373-20412/com.example.coroutinestest D/kobe: CoroutineExceptionHandler

    按照前面的逻辑异常虽然捕获了，但是一个子协程的异常会影响另一个子协程的运行，所以日志打不全

     *
     */
    private val handler7 = CoroutineExceptionHandler { _, _ ->
        Log.d("kobe", "CoroutineExceptionHandler")
    }

    private fun coroutineBuildRunBlock7() = runBlocking(Dispatchers.IO) {
        CoroutineScope(Job() + handler7)
            .launch {
                launch {
                    Log.d("kobe", "start job1 delay")
                    delay(1000)
                    Log.d("kobe", "end job1 delay")
                }
                launch {
                    Log.d("kobe", "job2 throw execption")
                    throw NullPointerException()
                }
            }
    }


    /**
     * 不会崩溃
     *
    2022-03-22 15:48:07.384 21777-21818/com.example.coroutinestest D/kobe: start job1 delay
    2022-03-22 15:48:07.384 21777-21820/com.example.coroutinestest D/kobe: job2 throw execption
    2022-03-22 15:48:07.385 21777-21820/com.example.coroutinestest D/kobe: CoroutineExceptionHandler
    2022-03-22 15:48:08.391 21777-21818/com.example.coroutinestest D/kobe: end job1 delay
    2022-03-22 15:48:09.389 21777-21818/com.example.coroutinestest D/kobe: start job3 delay

    按照前面的逻辑异常捕获了，使用了supervisorScope所以一个子协程的异常不会会影响另一个子协程的运行,并且不会影响这个域外的兄弟协程，所以日志全
     *
     */
    private val handler8 = CoroutineExceptionHandler { _, _ ->
        Log.d("kobe", "CoroutineExceptionHandler")
    }

    private fun coroutineBuildRunBlock8() = runBlocking(Dispatchers.IO) {
        CoroutineScope(Job() + handler8)
            .launch {
                launch {
                    delay(2000)
                    Log.d("kobe", "start job3 delay")
                }
                supervisorScope {
                    launch {
                        Log.d("kobe", "start job1 delay")
                        delay(1000)
                        Log.d("kobe", "end job1 delay")
                    }
                    launch {
                        Log.d("kobe", "job2 throw execption")
                        throw NullPointerException()
                    }
                }
            }
    }


    /**
     * 不会崩溃
     *
    2022-03-22 15:33:35.334 21018-21073/com.example.coroutinestest D/kobe: job2 throw execption
    2022-03-22 15:33:35.335 21018-21077/com.example.coroutinestest D/kobe: start job1 delay
    2022-03-22 15:33:35.339 21018-21077/com.example.coroutinestest D/kobe: CoroutineExceptionHandler

    SupervisorJob这个任务是阻止异常不会向外传播，因此不会影响其父亲/兄弟协程，也不会被其兄弟协程抛出的异常影响，但是他内部生成的各种协程是依然会像job一样互相影响，所以日志里面不全
     *
     */

    private val supervisorJob9 = SupervisorJob()
    private val handler9 = CoroutineExceptionHandler { _, _ ->
        Log.d("kobe", "CoroutineExceptionHandler")
    }

    private fun coroutineBuildRunBlock9() = runBlocking(Dispatchers.IO) {
        CoroutineScope(handler9 + supervisorJob9)
            .launch {
                launch {
                    Log.d("kobe", "start job1 delay")
                    delay(1000)
                    Log.d("kobe", "end job1 delay")
                }
                launch {
                    Log.d("kobe", "job2 throw execption")
                    throw NullPointerException()
                }
            }
    }


    /**
     * 不会崩溃
     *
    2022-03-22 15:45:20.807 21611-21653/com.example.coroutinestest D/kobe: start job1 delay
    2022-03-22 15:45:20.809 21611-21652/com.example.coroutinestest D/kobe: start job2 delay
    2022-03-22 15:45:20.814 21611-21651/com.example.coroutinestest D/kobe: start job3 delay
    2022-03-22 15:45:20.815 21611-21654/com.example.coroutinestest D/kobe: job4 throw execption
    2022-03-22 15:45:20.817 21611-21654/com.example.coroutinestest D/kobe: CoroutineExceptionHandler
    2022-03-22 15:45:21.820 21611-21654/com.example.coroutinestest D/kobe: end job1 delay
    2022-03-22 15:45:21.820 21611-21651/com.example.coroutinestest D/kobe: end job2 delay

    SupervisorJob这个任务是阻止异常不会向外传播，因此不会影响其父亲/兄弟协程，也不会被其兄弟协程抛出的异常影响，但是他内部生成的各种协程是依然会像job一样互相影响
     *
     */

    private val supervisorJob10 = SupervisorJob()
    private val handler10 = CoroutineExceptionHandler { _, _ ->
        Log.d("kobe", "CoroutineExceptionHandler")
    }

    private val coroutineContext10 = handler10 + supervisorJob10


    private fun coroutineBuildRunBlock10() = runBlocking(Dispatchers.IO) {
        CoroutineScope(coroutineContext10)
            .launch {
                launch {
                    Log.d("kobe", "start job1 delay")
                    delay(1000)
                    Log.d("kobe", "end job1 delay")
                }
                launch {
                    Log.d("kobe", "start job2 delay")
                    delay(1000)
                    Log.d("kobe", "end job2 delay")
                }

                CoroutineScope(coroutineContext10).launch {
                    launch {
                        Log.d("kobe", "start job3 delay")
                        delay(1000)
                        Log.d("kobe", "end job3 delay")
                    }
                    launch {
                        Log.d("kobe", "job4 throw execption")
                        throw NullPointerException()
                    }
                }
            }
    }


}