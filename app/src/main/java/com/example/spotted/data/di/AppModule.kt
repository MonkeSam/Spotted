package com.example.spotted.data.di

import com.example.spotted.data.repository.*
import com.example.spotted.data.view.*
import com.example.spotted.viewmodel.ProfileViewModel
import org.koin.core.module.dsl.*
import org.koin.dsl.module

val appModule = module {
    // Repository (tutti singleton)
    single { UserRepository() }
    single { CategoryRepository() }
    single { FollowRepository() }        // da usare anche altrove
    single { DiscardedRepository() }     // da usare anche altrove
    single { PostRepository(get(), get()) }  // passiamo FollowRepository e DiscardedRepository
    single { MessageRepository() }
    single { StorageRepository() }

    // ViewModel (factory) – ora li inizializzeremo con le dipendenze
    viewModel { ProfileViewModel(get()) }
    viewModel { ShareViewModel(get(), get(), get()) } // Category, Post, Storage
    viewModel { ChatViewModel(get(), get()) }         // Message, User
    viewModel { LoginViewModel(get()) }
    viewModel { SignupViewModel(get()) }
    viewModel { FollowingViewModel(get(), get(), get()) } // Follow, Message, Category
    viewModel { FeedViewModel(get(), get(), get(), get()) } // Post, Follow, Discarded, Category
    viewModel { MapViewModel(get()) }                    // Follow
}