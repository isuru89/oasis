#!/bin/bash

do_log() {
  echo "==============================================================================="
  echo "$1"
  echo "==============================================================================="
}

proj_build_status() {
  case $1 in
    'events-api') echo 'true';;
    'stats-api') echo 'true';;
    'engine') echo 'true';;
    'feeder') echo 'true';;
    *) echo 'true'
  esac
}
skip_build_project=false
do_clean=true
skip_tests=true
build_base_java_image=true
compose_hide_logs="kafka,zookeeper"

# ///////////////////// BUILD PROJECT ////////////////////////////////////

if [ "$skip_build_project" != "true" ]; then
  MVN_ARGS=""
  if [ "$do_clean" == "true" ]; then
    MVN_ARGS+="clean"
  fi
  MVN_ARGS+=" install "
  if [ "$skip_tests" == "true" ]; then
    MVN_ARGS+=" -DskipTests "
  fi

  do_log "⚙️  Building Oasis..."
  mvn $MVN_ARGS
else
  echo "🔸 Skipped: Building maven project"
fi

# ///////////////////// BUILD DOCKER IMAGES ////////////////////////////////////

do_log "🚢 Creating Containers..."

if [ "$build_base_java_image" == "true" ]; then
  do_log "🛠️  Build the base java image"
  docker build -t oasis/base-java -f ./buildscripts/docker/base-java.dockerfile .
else
  echo "🔸 Skipped: Building base java image"
fi

cp externals/kafka-stream/target/libs/* buildscripts/modules
cp externals/kafka-stream/target/oasis-ext-kafkastream.jar buildscripts/modules

if [ "$(proj_build_status events-api)" == "true" ]; then
  do_log "🛠️  Building Events API Docker Image..."
  cd services/events-api || exit 1
  docker build -t oasis/events-api .
  cd ../..
else
  echo "🔸 Skipped: Building events-api"
fi

if [ "$(proj_build_status stats-api)" == "true" ]; then
  do_log "🛠  Building Admin/Stats API Docker Image..."
  cd services/stats-api || exit 1
  docker build -t oasis/stats-api .
  cd ../..
else
  echo "🔸 Skipped: Building stats-api"
fi

if [ "$(proj_build_status feeder)" == "true" ]; then
  do_log "🛠️  Building Feeder Docker Image..."
  cd services/feeder || exit 1
  docker build -t oasis/feeder .
  cd ../..
else
  echo "🔸 Skipped: Building feeder"
fi

if [ "$(proj_build_status engine)" == "true" ]; then
  do_log "🛠️  Building Engine Docker Image..."
  cd engine || exit 1
  docker build -t oasis/engine .
  cd ..
else
  echo "🔸 Skipped: Building engine"
fi

mkdir -p .tmpdata/enginedb
mkdir -p .tmpdata/cache


do_log "📣 Starting Oasis..."

IFS=',' read -ra LOGS <<< "$compose_hide_logs"
args_logs=""
for log in "${LOGS[@]}"; do
  args_logs+="--no-attach $log "
done

docker compose up $args_logs



