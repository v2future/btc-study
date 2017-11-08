/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.db;

import net.bither.bitherj.db.*;

public class DesktopDbImpl extends AbstractDb {
    @Override
    public IBlockProvider initBlockProvider() {
        return BlockProvider.getInstance();
    }

    @Override
    public IPeerProvider initPeerProvider() {
        return PeerProvider.getInstance();
    }

    @Override
    public ITxProvider initTxProvider() {
        return TxProvider.getInstance();
    }

    @Override
    public IAddressProvider initAddressProvider() {
        return AddressProvider.getInstance();
    }

    @Override
    public IHDAccountProvider initHDAccountProvider() {
        return HDAccountProvider.getInstance();
    }

	@Override
	public IHDAccountAddressProvider initHDAccountAddressProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEnterpriseHDMProvider initEnterpriseHDMProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDesktopAddressProvider initEnDesktopAddressProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDesktopTxProvider initDesktopTxProvider() {
		// TODO Auto-generated method stub
		return null;
	}
}
