#!/bin/bash

# Exit on error
set -e

echo "Building and publishing Docker image..."
./gradlew :server-docker:publishImage

echo "Deploying to Cloud Run..."
PROJECT_ID=$(gcloud config get-value project)

gcloud run deploy flashcards-server \
    --image gcr.io/$PROJECT_ID/flashcards-server \
    --platform managed \
    --region us-central1 \
    --allow-unauthenticated \
    --set-secrets=GOOGLE_OAUTH_CLIENT_ID=google-oauth-client-id:latest,GOOGLE_OAUTH_CLIENT_SECRET=google-oauth-client-secret:latest,GOOGLE_OAUTH_TEST_CLIENT_ID=google-oauth-test-client-id:latest,GOOGLE_OAUTH_TEST_CLIENT_SECRET=google-oauth-test-client-secret:latest,GEMINI_API_KEY=gemini-api-key:latest \
    --set-env-vars=GOOGLE_OAUTH_REDIRECT_URI=https://api.flashcards.solenne.ai/api/v1/auth/google/callback,GOOGLE_OAUTH_TEST_REDIRECT_URI=https://api.flashcards.solenne.ai/api/v1/auth/google/test/callback,FIRESTORE_PROJECT_ID=$PROJECT_ID

echo "Deployment complete!"
