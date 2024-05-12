package com.unicornutterances.cms.auth

import com.unicornutterances.cms.github.client.GitHubClient
import com.unicornutterances.cms.github.client.GitHubClientImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    single { GitHubClientImpl() } bind GitHubClient::class
    singleOf(::GitHubOAuthService)
}
