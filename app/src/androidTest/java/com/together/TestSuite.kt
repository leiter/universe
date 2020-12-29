package com.together

import com.together.authentication.LoginCases
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(LoginCases::class)
class TestSuite