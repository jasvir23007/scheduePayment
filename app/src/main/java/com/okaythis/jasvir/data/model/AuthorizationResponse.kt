package com.okaythis.jasvir.data.model

import com.okaythis.jasvir.data.model.Status

data class AuthorizationResponse(val sessionExternalId: String, val status: Status)