import React, { Component } from 'react'
import styled from 'styled-components'
import Avatar from '../../components/Avatar'
import profileImg from '../../profile.jpg'
import Panel from '../../components/Panel';
import ImageTitleValue, {ImageContent} from '../../components/ImageTitleValue';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import PointLabel from '../../components/PointLabel';
import BadgeView from '../../components/BadgeView';
import TeamHistoryRecord from './TeamHistoryRecord';

const Content = styled.div`
  height: 100%;
`

const UserHeader = styled.div`
  width: 100%;
  height: 50px;
  line-height: 50px;
  background-color: #53A5D0;
  color: #FBFCFD;
  font-size: 1.61em;
  letter-spacing: 1.2px;
`

const Title = styled.div`
  padding: 0 20px;
  font-weight: 700;
`

const UserContent = styled.div`
  display: flex;
  width: 100%;
  height: 100%;
`

const Column1 = styled.div`
  flex: 1;
  text-align: center;
  background-color: #1E2B34;
  padding: 10px 5px;
`

const Column2 = styled.div`
  flex: 2.5;
`

const UserNameTitle = styled.div`
  margin-top: 10px;
  text-transform: uppercase;
  font-size: 24px;
  letter-spacing: 1.5px;
  font-weight: 600;
  line-height: 40px;
`

const UserDesignation = styled.div`
  border-bottom: 3px solid #ffffff33;
  line-height: 14px;
  padding-bottom: 10px;
  color: #ddd;
  font-size: 12px;
  letter-spacing: 1.7px;
  font-weight: 400;
`

const UserProfileHeader = styled.div`
  border-top: 1px solid #ffffff22;
  background-color: #20465E;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-around;
`


export default class ProfilePage extends Component {
  render() {
    return (
      <Content>
        <UserContent>
          <Column1>
            <Avatar image={profileImg} size={164} />
            <UserNameTitle>John Doe</UserNameTitle>
            <UserDesignation>Senior Engineer</UserDesignation>

            <ImageTitleValue image={<FontAwesomeIcon icon="football-ball" />}
              title="Team:" 
              value="QA-Testings" />
            <ImageTitleValue image={<FontAwesomeIcon icon="at" />}
              title="john@product.com" />

          </Column1>
          <Column2>
            <UserProfileHeader>
              <ImageContent color="#fff" imgColor="gold" image={<FontAwesomeIcon icon="coins" />}>
                <PointLabel color="gold" value={(2345).toLocaleString()} delta={432} annotation="THIS WEEK" />
              </ImageContent>
              <ImageContent color="#fff" imgColor="#ff7f82" image={<FontAwesomeIcon icon="award" />}>
                <PointLabel color="#ff7f82" value={14} />
              </ImageContent>
              <ImageContent color="#fff" imgColor="#72e5b7" image={<FontAwesomeIcon icon="map-marker-alt" />}>
                <PointLabel color="#72e5b7" value={'73%'} />
              </ImageContent>
              <ImageContent color="#fff" imgColor="#ec8c54" image={<FontAwesomeIcon icon="trophy" />}>
                <PointLabel color="#ec8c54" value={8} />
              </ImageContent>
            </UserProfileHeader>
            
            <Panel title="MY BADGES (5)">
              <div><BadgeView /></div>
            </Panel>
            <Panel title="TEAM HISTORY">
              <TeamHistoryRecord team="Team-1" active={true} />
              <TeamHistoryRecord />
              <TeamHistoryRecord />
            </Panel>
          </Column2>
        </UserContent>
      </Content>
    )
  }
}
