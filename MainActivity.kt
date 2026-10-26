
package org.apz.gamevault

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity:AppCompatActivity(){
    private lateinit var session:SessionStore; private lateinit var api:Api
    private lateinit var server:EditText; private lateinit var user:EditText; private lateinit var pass:EditText
    private lateinit var loginPanel:View; private lateinit var library:View; private lateinit var search:EditText
    private lateinit var adapter:GameAdapter

    override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main)
        session=SessionStore(this);api=Api(session)
        server=findViewById(R.id.server);user=findViewById(R.id.username);pass=findViewById(R.id.password)
        loginPanel=findViewById(R.id.loginPanel);library=findViewById(R.id.libraryPanel);search=findViewById(R.id.search)
        adapter=GameAdapter(emptyList()){g->download(g)}
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.games).apply{
            layoutManager=LinearLayoutManager(this@MainActivity);adapter=this@MainActivity.adapter
        }
        findViewById<Button>(R.id.login).setOnClickListener{doLogin()}
        findViewById<Button>(R.id.searchButton).setOnClickListener{load(search.text.toString())}
        findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.refresh).setOnRefreshListener{
            load(search.text.toString())
            findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.refresh).isRefreshing=false
        }
        findViewById<Button>(R.id.langButton).setOnClickListener{toggleLanguage()}
        session.server?.let{server.setText(it)}
        if(session.access!=null) showLibrary()
    }
    private fun doLogin(){
        val s=server.text.toString().trim().removeSuffix("/"); if(s.isBlank())return toast("Server required")
        session.server=s
        api.login(user.text.toString(),pass.text.toString()){ok,msg->runOnUiThread{
            if(ok){toast("Login successful");showLibrary()}else toast(msg)
        }}
    }
    private fun showLibrary(){loginPanel.visibility=View.GONE;library.visibility=View.VISIBLE;load(null)}
    private fun load(q:String?){api.games(q){list,err->runOnUiThread{
        if(err!=null){toast(err);return@runOnUiThread};adapter.submit(list)
    }}}
    private fun download(g:Game){api.download(g.id){ok,msg->runOnUiThread{toast("${g.name}: $msg")}}}
    private fun toggleLanguage(){
        val fa=findViewById<Button>(R.id.langButton).text=="FA"
        findViewById<Button>(R.id.langButton).text=if(fa)"EN" else "FA"
        findViewById(R.id.title).let{(it as TextView).text=if(fa)"گیم‌ولت اندروید" else "GameVault Android"}
        findViewById<Button>(R.id.login).text=if(fa)"ورود" else "Login"
        search.hint=if(fa)"جستجوی بازی‌ها..." else "Search games..."
    }
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
