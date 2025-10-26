#!/bin/bash

# Exit on error
set -e

echo "Building and publishing Docker image..."
./gradlew :server-docker:publishImage

echo "Deploying to Cloud Run..."
gcloud run deploy flashcards-server \
    --image gcr.io/$(gcloud config get-value project)/flashcards-server \
    --platform managed \
    --region us-central1 \
    --allow-unauthenticated \
    --set-secrets=GOOGLE_OAUTH_CLIENT_ID=google-oauth-client-id:latest,GOOGLE_OAUTH_CLIENT_SECRET=google-oauth-client-secret:latest,GEMINI_API_KEY=gemini-api-key:latest \
    --set-env-vars=GOOGLE_OAUTH_REDIRECT_URI=https://api.flashcards.solenne.ai/api/v1/auth/google/callback

echo "Deployment complete!"
