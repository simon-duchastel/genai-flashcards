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
    --allow-unauthenticated

echo "Deployment complete!"
