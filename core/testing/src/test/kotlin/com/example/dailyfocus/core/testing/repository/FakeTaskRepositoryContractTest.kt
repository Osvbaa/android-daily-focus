package com.example.dailyfocus.core.testing.repository

class FakeTaskRepositoryContractTest : TaskRepositoryContract() {
    override val repository = FakeTaskRepository()
}
