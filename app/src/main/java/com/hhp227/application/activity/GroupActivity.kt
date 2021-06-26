package com.hhp227.application.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.hhp227.application.R
import com.hhp227.application.fragment.TabHostLayoutFragment

class GroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val groupId = intent.getIntExtra("group_id", 0)
        val authorId = intent.getIntExtra("author_id", 0)
        val groupName = intent.getStringExtra("group_name")

        setContentView(R.layout.activity_group)
        (TabHostLayoutFragment.newInstance(groupId, authorId, groupName) as TabHostLayoutFragment).also { fragMain ->
            supportFragmentManager.beginTransaction().replace(R.id.contentFrame, fragMain).commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        R.id.actionChat -> {
            startActivity(Intent(this, ChatActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
