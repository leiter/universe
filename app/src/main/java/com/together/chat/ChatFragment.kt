package com.together.chat

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ChatFragment : Fragment() {



}



const val CURRENT_CHAT = 0
const val OLD_CHAT_LIST = 1
const val CHAT_FRIENDS = 2

class ChatCategoryAdapter(fm:FragmentManager) : FragmentStatePagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {  //@FragmentPosition
        when (position) {
            CURRENT_CHAT -> return OpenChatsFragment()
            OLD_CHAT_LIST -> return OldChatsFragment()
            CHAT_FRIENDS -> return ChatContactFragment()


            else -> throw IllegalStateException(
                "No Fragment on this pager for ${position}.")
        }

    }

    override fun getCount(): Int = 3
}