/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.tuanictu97.exo.smoothstreaming.offline;

import android.net.Uri;
import io.github.tuanictu97.exo.core.C;
import io.github.tuanictu97.exo.core.offline.DownloaderConstructorHelper;
import io.github.tuanictu97.exo.core.offline.SegmentDownloader;
import io.github.tuanictu97.exo.core.offline.StreamKey;
import io.github.tuanictu97.exo.smoothstreaming.manifest.SsManifest;
import io.github.tuanictu97.exo.smoothstreaming.manifest.SsManifest.StreamElement;
import io.github.tuanictu97.exo.smoothstreaming.manifest.SsManifestParser;
import io.github.tuanictu97.exo.smoothstreaming.manifest.SsUtil;
import io.github.tuanictu97.exo.core.upstream.DataSource;
import io.github.tuanictu97.exo.core.upstream.DataSpec;
import io.github.tuanictu97.exo.core.upstream.ParsingLoadable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A downloader for SmoothStreaming streams.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * SimpleCache cache = new SimpleCache(downloadFolder, new NoOpCacheEvictor(), databaseProvider);
 * DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory("ExoPlayer", null);
 * DownloaderConstructorHelper constructorHelper =
 *     new DownloaderConstructorHelper(cache, factory);
 * // Create a downloader for the first track of the first stream element.
 * SsDownloader ssDownloader =
 *     new SsDownloader(
 *         manifestUrl,
 *         Collections.singletonList(new StreamKey(0, 0)),
 *         constructorHelper);
 * // Perform the download.
 * ssDownloader.download(progressListener);
 * // Access downloaded data using CacheDataSource
 * CacheDataSource cacheDataSource =
 *     new CacheDataSource(cache, factory.createDataSource(), CacheDataSource.FLAG_BLOCK_ON_CACHE);
 * }</pre>
 */
public final class SsDownloader extends SegmentDownloader<SsManifest> {

  /**
   * @param manifestUri The {@link Uri} of the manifest to be downloaded.
   * @param streamKeys Keys defining which streams in the manifest should be selected for download.
   *     If empty, all streams are downloaded.
   * @param constructorHelper A {@link DownloaderConstructorHelper} instance.
   */
  public SsDownloader(
      Uri manifestUri, List<StreamKey> streamKeys, DownloaderConstructorHelper constructorHelper) {
    super(SsUtil.fixManifestUri(manifestUri), streamKeys, constructorHelper);
  }

  @Override
  protected SsManifest getManifest(DataSource dataSource, DataSpec dataSpec) throws IOException {
    return ParsingLoadable.load(dataSource, new SsManifestParser(), dataSpec, C.DATA_TYPE_MANIFEST);
  }

  @Override
  protected List<Segment> getSegments(
      DataSource dataSource, SsManifest manifest, boolean allowIncompleteList) {
    ArrayList<Segment> segments = new ArrayList<>();
    for (StreamElement streamElement : manifest.streamElements) {
      for (int i = 0; i < streamElement.formats.length; i++) {
        for (int j = 0; j < streamElement.chunkCount; j++) {
          segments.add(
              new Segment(
                  streamElement.getStartTimeUs(j),
                  new DataSpec(streamElement.buildRequestUri(i, j))));
        }
      }
    }
    return segments;
  }

}
