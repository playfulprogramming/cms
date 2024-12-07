package com.playfulprogramming.cms.auth

import com.playfulprogramming.cms.github.client.GitHubClient
import com.playfulprogramming.cms.github.client.GitHubClientImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    single { GitHubClientImpl() } bind GitHubClient::class
    singleOf(::GitHubOAuthService)
}
