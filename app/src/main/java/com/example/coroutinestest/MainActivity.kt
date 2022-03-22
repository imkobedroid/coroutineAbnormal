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
//        loadData1()
//        loadData2()
//        loadData3()
//        loadData4()
//        loadData5()


//        coroutineBuildRunBlock7()
        coroutineBuildRunBlock8()
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